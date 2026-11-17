
package com.example.event_mgm.ServiceImpl.export;

import com.example.event_mgm.Repository.event.EventRepository;

import com.example.event_mgm.Repository.repository.RegistrationRepository;
import com.example.event_mgm.dto.request.ParticipantExportRequestDto;
import com.example.event_mgm.enums.ParticipantExportField;
import com.example.event_mgm.model.Event;
import com.example.event_mgm.model.Registration;
import com.example.event_mgm.model.TeamMember;
import com.example.event_mgm.service.export.ParticipantExportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipantExportServiceImpl
        implements ParticipantExportService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @Override
    @Transactional(readOnly = true)
    public byte[] exportParticipants(
            ParticipantExportRequestDto request) {

        validateRequest(request);

        List<Registration> registrations;

        if (request.getExportType()
                == ParticipantExportRequestDto.ExportType.EVENT) {

            Long eventId = request.getEventId();

            if (!eventRepository.existsById(eventId)) {
                throw new IllegalArgumentException(
                        "Event not found with ID: " + eventId
                );
            }

            registrations =
                    registrationRepository.findAllForEventExport(eventId);

        } else {

            registrations =
                    registrationRepository.findAllForOverallExport();
        }

        // Preserve frontend field selection order.
        List<ParticipantExportField> fields =
                new ArrayList<>(
                        new LinkedHashSet<>(request.getSelectedFields())
                );

        return generateExcel(registrations, fields);
    }

    private void validateRequest(
            ParticipantExportRequestDto request) {

        if (request == null || request.getExportType() == null) {
            throw new IllegalArgumentException(
                    "Export type is required"
            );
        }

        if (request.getSelectedFields() == null
                || request.getSelectedFields().isEmpty()) {
            throw new IllegalArgumentException(
                    "Select at least one export field"
            );
        }

        if (request.getExportType()
                == ParticipantExportRequestDto.ExportType.EVENT
                && request.getEventId() == null) {

            throw new IllegalArgumentException(
                    "Event ID is required for event-wise export"
            );
        }

        if (request.getExportType()
                == ParticipantExportRequestDto.ExportType.ALL
                && request.getEventId() != null) {

            throw new IllegalArgumentException(
                    "Event ID must not be provided for overall export"
            );
        }
    }

    private byte[] generateExcel(
            List<Registration> registrations,
            List<ParticipantExportField> fields) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Participants");

            createHeader(sheet, workbook, fields);

            int rowNumber = 1;
            int serialNumber = 1;

            for (Registration registration : registrations) {

                Event event = registration.getEvent();

                List<TeamMember> teamMembers =
                        registration.getTeamMembers() == null
                                ? List.of()
                                : registration.getTeamMembers();

                // Primary registrant / team leader
                String leaderType = teamMembers.isEmpty()
                        ? "SOLO"
                        : "TEAM_LEADER";

                writeParticipantRow(
                        sheet,
                        rowNumber++,
                        serialNumber++,
                        fields,
                        registration,
                        event,
                        registration,
                        leaderType
                );

                // Additional team members
                for (TeamMember member : teamMembers) {

                    writeParticipantRow(
                            sheet,
                            rowNumber++,
                            serialNumber++,
                            fields,
                            registration,
                            event,
                            member,
                            "TEAM_MEMBER"
                    );
                }
            }

            // Formatting
            for (int i = 0; i < fields.size(); i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(
                        i,
                        Math.min(sheet.getColumnWidth(i), 15000)
                );
            }

            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(
                    new org.apache.poi.ss.util.CellRangeAddress(
                            0,
                            Math.max(0, rowNumber - 1),
                            0,
                            fields.size() - 1
                    )
            );

            workbook.write(output);

            log.info(
                    "Participant export generated. Registrations: {}, Participants: {}",
                    registrations.size(),
                    rowNumber - 1
            );

            return output.toByteArray();

        } catch (IOException e) {

            log.error("Participant Excel generation failed", e);

            throw new IllegalStateException(
                    "Unable to generate participant Excel file"
            );
        }
    }

    private void createHeader(
            Sheet sheet,
            Workbook workbook,
            List<ParticipantExportField> fields) {

        Row header = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        style.setFont(font);
        style.setFillForegroundColor(
                IndexedColors.DARK_BLUE.getIndex()
        );
        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        style.setAlignment(HorizontalAlignment.CENTER);

        for (int i = 0; i < fields.size(); i++) {

            Cell cell = header.createCell(i);

            cell.setCellValue(getFieldLabel(fields.get(i)));
            cell.setCellStyle(style);
        }
    }

    private void writeParticipantRow(
            Sheet sheet,
            int rowNumber,
            int serialNumber,
            List<ParticipantExportField> fields,
            Registration registration,
            Event event,
            Object participant,
            String participantType) {

        Row row = sheet.createRow(rowNumber);

        for (int i = 0; i < fields.size(); i++) {

            ParticipantExportField field = fields.get(i);

            Object value = getFieldValue(
                    field,
                    registration,
                    event,
                    participant,
                    participantType
            );

            Cell cell = row.createCell(i);

            if (field == ParticipantExportField.REGISTRATION_ID
                    || field == ParticipantExportField.EVENT_ID) {
                setNumericCell(cell, value);
            } else if (value instanceof Boolean booleanValue) {
                cell.setCellValue(booleanValue);
            } else if (value instanceof Number number) {
                cell.setCellValue(number.doubleValue());
            } else {
                cell.setCellValue(
                        value == null ? "" : value.toString()
                );
            }
        }
    }

    private Object getFieldValue(
            ParticipantExportField field,
            Registration registration,
            Event event,
            Object participant,
            String participantType) {

        boolean isTeamMember = participant instanceof TeamMember;

        TeamMember member = isTeamMember
                ? (TeamMember) participant
                : null;

        switch (field) {

            // Event information
            case EVENT_ID:
                return event.getId();

            case EVENT_NAME:
                return event.getEventName();

            case CATEGORY:
                return event.getCategory() != null
                        ? event.getCategory().getName() : "";

            case EVENT_TYPE:
                return event.getEventType() != null
                        ? event.getEventType().getName() : "";

            case EVENT_DATE:
                return formatDate(event.getEventDateTime());

            case VENUE:
                return event.getVenue();

            // Registration information
            case REGISTRATION_ID:
                return registration.getId();

            case REGISTRATION_CODE:
                return registration.getUniqueCode();

            case REGISTRATION_DATE:
                return formatDate(registration.getRegisteredAt());

            case REGISTRATION_TYPE:
                return registration.getRegistrationType();

            case REGISTRATION_STATUS:
                return registration.getRegistrationStatus();

            case PAYMENT_STATUS:
                return registration.getPaymentStatus();

            case AMOUNT_DUE:
                return registration.getAmountDue();

            // Participant information
            case FIRST_NAME:
                return isTeamMember
                        ? member.getFirstName()
                        : registration.getFirstName();

            case MIDDLE_NAME:
                return isTeamMember
                        ? member.getMiddleName()
                        : registration.getMiddleName();

            case LAST_NAME:
                return isTeamMember
                        ? member.getLastName()
                        : registration.getLastName();

            case EMAIL:
                return isTeamMember
                        ? member.getEmail()
                        : registration.getEmail();

            case PHONE_NUMBER:
                return isTeamMember
                        ? member.getPhoneNumber()
                        : registration.getPhoneNumber();

            case COLLEGE_NAME:
                return registration.getCollegeName();

            case COURSE:
                return registration.getCourse();

            case PARTICIPANT_TYPE:
                return participantType;

            // Additional information
            case NUMBER_OF_MEMBERS:
                return registration.getNumberOfMembers();

            case EVENT_FEE:
                return event.getEventFee();

            case EVENT_ACTIVE_STATUS:
                return event.getIsActive();

            case REGISTRATION_ACTIVE_STATUS:
                return registration.getIsActive();

            case PARTICIPANT_ACTIVE_STATUS:
                return isTeamMember
                        ? member.getIsActive()
                        : registration.getIsActive();

            default:
                return "";
        }
    }

    private String getFieldLabel(ParticipantExportField field) {

        return switch (field) {

            case EVENT_ID -> "Event ID";
            case EVENT_NAME -> "Event Name";
            case CATEGORY -> "Category";
            case EVENT_TYPE -> "Event Type";
            case EVENT_DATE -> "Event Date";
            case VENUE -> "Venue";

            case REGISTRATION_ID -> "Registration ID";
            case REGISTRATION_CODE -> "Registration Code";
            case REGISTRATION_DATE -> "Registration Date";
            case REGISTRATION_TYPE -> "Registration Type";
            case REGISTRATION_STATUS -> "Registration Status";
            case PAYMENT_STATUS -> "Payment Status";
            case AMOUNT_DUE -> "Amount Due";

            case FIRST_NAME -> "First Name";
            case MIDDLE_NAME -> "Middle Name";
            case LAST_NAME -> "Last Name";
            case EMAIL -> "Email";
            case PHONE_NUMBER -> "Phone Number";
            case COLLEGE_NAME -> "College Name";
            case COURSE -> "Course";
            case PARTICIPANT_TYPE -> "Participant Type";

            case NUMBER_OF_MEMBERS -> "Number of Members";
            case EVENT_FEE -> "Event Fee";
            case EVENT_ACTIVE_STATUS -> "Event Active";
            case REGISTRATION_ACTIVE_STATUS -> "Registration Active";
            case PARTICIPANT_ACTIVE_STATUS -> "Participant Active";
        };
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null
                ? ""
                : dateTime.format(DATE_FORMAT);
    }

    private void setNumericCell(Cell cell, Object value) {

        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(value == null ? "" : value.toString());
        }
    }
}
