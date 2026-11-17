package com.example.event_mgm.service.registration;

import com.example.event_mgm.Repository.event.EventRepository;
import com.example.event_mgm.Repository.repository.RegistrationRepository;
import com.example.event_mgm.Repository.repository.TeamMemberRepository;
import com.example.event_mgm.enums.PaymentStatus;
import com.example.event_mgm.enums.RegistrationStatus;
import com.example.event_mgm.exceptions.InvalidData;
import com.example.event_mgm.exceptions.ResourceNotFoundException;
import com.example.event_mgm.model.Event;
import com.example.event_mgm.model.Registration;
import com.example.event_mgm.model.TeamMember;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipantExportService {

    private static final String[] HEADERS = {
            "Event", "Registration code", "Registration type", "Participant role",
            "First name", "Middle name", "Last name", "Email", "Phone",
            "College", "Course", "Registered at", "Registration status",
            "Payment status", "Amount due (registration)"
    };

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional(readOnly = true)
    public byte[] export(
            List<Long> requestedEventIds,
            PaymentStatus paymentStatus,
            RegistrationStatus registrationStatus,
            boolean includeTeamMembers) {
        List<Event> events = findSelectedEvents(requestedEventIds);
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        List<Registration> registrations = registrationRepository
                .findByEventIdInAndIsActiveTrueOrderByEventIdAscRegisteredAtAsc(eventIds)
                .stream()
                .filter(registration -> paymentStatus == null
                        || paymentStatus.name().equals(registration.getPaymentStatus()))
                .filter(registration -> registrationStatus == null
                        || registrationStatus.name().equals(registration.getRegistrationStatus()))
                .toList();

        Map<Long, List<TeamMember>> teamMembersByRegistration = loadTeamMembers(registrations, includeTeamMembers);
        Map<Long, List<ParticipantRow>> rowsByEvent = new LinkedHashMap<>();
        for (Event event : events) {
            rowsByEvent.put(event.getId(), new ArrayList<>());
        }
        for (Registration registration : registrations) {
            List<ParticipantRow> rows = rowsByEvent.get(registration.getEvent().getId());
            rows.add(toParticipantRow(registration, (TeamMember) null));
            for (TeamMember member : teamMembersByRegistration.getOrDefault(registration.getId(), List.of())) {
                rows.add(toParticipantRow(registration, member));
            }
        }

        return createWorkbook(events, rowsByEvent, registrations.size(), paymentStatus, registrationStatus);
    }

    private List<Event> findSelectedEvents(List<Long> requestedEventIds) {
        if (requestedEventIds == null || requestedEventIds.isEmpty()) {
            List<Event> activeEvents = eventRepository.findByIsActiveTrue().stream()
                    .sorted(Comparator.comparing(Event::getEventName, String.CASE_INSENSITIVE_ORDER))
                    .toList();
            if (activeEvents.isEmpty()) {
                throw new ResourceNotFoundException("No active events are available to export");
            }
            return activeEvents;
        }

        List<Long> uniqueIds = requestedEventIds.stream().distinct().toList();
        List<Event> events = eventRepository.findByIdInAndIsActiveTrueOrderByEventNameAsc(uniqueIds);
        if (events.size() != uniqueIds.size()) {
            throw new InvalidData("One or more selected events are missing or inactive. Refresh the event list.");
        }
        return events;
    }

    private Map<Long, List<TeamMember>> loadTeamMembers(
            List<Registration> registrations,
            boolean includeTeamMembers) {
        if (!includeTeamMembers || registrations.isEmpty()) {
            return Map.of();
        }
        List<Long> registrationIds = registrations.stream().map(Registration::getId).toList();
        return teamMemberRepository
                .findByRegistrationIdInAndIsActiveTrueOrderByRegistrationIdAscIdAsc(registrationIds)
                .stream()
                .collect(Collectors.groupingBy(
                        member -> member.getRegistration().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    private ParticipantRow toParticipantRow(Registration registration, TeamMember member) {
        boolean leader = member == null;
        return new ParticipantRow(
                registration.getEvent().getEventName(),
                registration.getUniqueCode() == null || registration.getUniqueCode().isBlank()
                        ? String.valueOf(registration.getId()) : registration.getUniqueCode(),
                registration.getRegistrationType(),
                leader ? "Leader" : "Team member",
                leader ? registration.getFirstName() : member.getFirstName(),
                leader ? registration.getMiddleName() : member.getMiddleName(),
                leader ? registration.getLastName() : member.getLastName(),
                leader ? registration.getEmail() : member.getEmail(),
                leader ? registration.getPhoneNumber() : member.getPhoneNumber(),
                registration.getCollegeName(),
                registration.getCourse(),
                registration.getRegisteredAt(),
                registration.getRegistrationStatus(),
                registration.getPaymentStatus(),
                leader ? registration.getAmountDue() : null);
    }

    private byte[] createWorkbook(
            List<Event> events,
            Map<Long, List<ParticipantRow>> rowsByEvent,
            int registrationCount,
            PaymentStatus paymentStatus,
            RegistrationStatus registrationStatus) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm"));
            CellStyle amountStyle = workbook.createCellStyle();
            amountStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

            createSummarySheet(workbook, events, rowsByEvent, registrationCount,
                    paymentStatus, registrationStatus, headerStyle);
            List<ParticipantRow> allRows = events.stream()
                    .flatMap(event -> rowsByEvent.getOrDefault(event.getId(), List.of()).stream())
                    .toList();
            createParticipantSheet(workbook, "All Participants", allRows, headerStyle, dateStyle, amountStyle);

            Set<String> usedSheetNames = new HashSet<>(Set.of("summary", "all participants"));
            for (Event event : events) {
                String sheetName = uniqueSheetName(event.getEventName(), usedSheetNames);
                createParticipantSheet(workbook, sheetName,
                        rowsByEvent.getOrDefault(event.getId(), List.of()),
                        headerStyle, dateStyle, amountStyle);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create the participant Excel workbook", exception);
        }
    }

    private void createSummarySheet(
            Workbook workbook,
            List<Event> events,
            Map<Long, List<ParticipantRow>> rowsByEvent,
            int registrationCount,
            PaymentStatus paymentStatus,
            RegistrationStatus registrationStatus,
            CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Summary");
        Row header = sheet.createRow(0);
        String[] columns = {"Event", "Event date", "Registrations", "Participants"};
        for (int column = 0; column < columns.length; column++) {
            Cell cell = header.createCell(column);
            cell.setCellValue(columns[column]);
            cell.setCellStyle(headerStyle);
        }
        int totalParticipants = 0;
        for (int index = 0; index < events.size(); index++) {
            Event event = events.get(index);
            List<ParticipantRow> eventRows = rowsByEvent.getOrDefault(event.getId(), List.of());
            totalParticipants += eventRows.size();
            Row row = sheet.createRow(index + 1);
            row.createCell(0).setCellValue(event.getEventName());
            row.createCell(1).setCellValue(event.getEventDateTime().toString());
            row.createCell(2).setCellValue(registrationsForEvent(eventRows));
            row.createCell(3).setCellValue(eventRows.size());
        }
        int filterRow = events.size() + 2;
        Row filtersHeader = sheet.createRow(filterRow);
        filtersHeader.createCell(0).setCellValue("Applied filters");
        filtersHeader.getCell(0).setCellStyle(headerStyle);
        sheet.createRow(filterRow + 1).createCell(0).setCellValue(
                "Payment status: " + (paymentStatus == null ? "All" : paymentStatus.name()));
        sheet.createRow(filterRow + 2).createCell(0).setCellValue(
                "Registration status: " + (registrationStatus == null ? "All" : registrationStatus.name()));
        sheet.createRow(filterRow + 3).createCell(0).setCellValue("Registration count: " + registrationCount);
        sheet.createRow(filterRow + 4).createCell(0).setCellValue("Participant rows: " + totalParticipants);

        sheet.setColumnWidth(0, 34 * 256);
        sheet.setColumnWidth(1, 23 * 256);
        sheet.setColumnWidth(2, 18 * 256);
        sheet.setColumnWidth(3, 18 * 256);
    }

    private int registrationsForEvent(List<ParticipantRow> rows) {
        return (int) rows.stream().filter(row -> "Leader".equals(row.participantRole())).count();
    }

    private void createParticipantSheet(
            Workbook workbook,
            String name,
            List<ParticipantRow> participants,
            CellStyle headerStyle,
            CellStyle dateStyle,
            CellStyle amountStyle) {
        Sheet sheet = workbook.createSheet(name);
        Row header = sheet.createRow(0);
        for (int column = 0; column < HEADERS.length; column++) {
            Cell cell = header.createCell(column);
            cell.setCellValue(HEADERS[column]);
            cell.setCellStyle(headerStyle);
        }
        for (int index = 0; index < participants.size(); index++) {
            ParticipantRow participant = participants.get(index);
            Row row = sheet.createRow(index + 1);
            String[] values = {
                    participant.eventName(), participant.registrationCode(), participant.registrationType(),
                    participant.participantRole(), participant.firstName(), participant.middleName(),
                    participant.lastName(), participant.email(), participant.phone(), participant.college(),
                    participant.course(), null, participant.registrationStatus(), participant.paymentStatus(), null
            };
            for (int column = 0; column < values.length; column++) {
                if (values[column] != null) {
                    row.createCell(column).setCellValue(values[column]);
                }
            }
            if (participant.registeredAt() != null) {
                Cell dateCell = row.createCell(11);
                dateCell.setCellValue(Timestamp.valueOf(participant.registeredAt()));
                dateCell.setCellStyle(dateStyle);
            }
            if (participant.amountDue() != null) {
                Cell amountCell = row.createCell(14);
                amountCell.setCellValue(participant.amountDue().doubleValue());
                amountCell.setCellStyle(amountStyle);
            }
        }
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(
                0, participants.size(), 0, HEADERS.length - 1));
        int[] widths = {25, 22, 18, 18, 18, 18, 18, 30, 18, 28, 22, 22, 22, 18, 24};
        for (int column = 0; column < widths.length; column++) {
            sheet.setColumnWidth(column, widths[column] * 256);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private String uniqueSheetName(String requestedName, Set<String> usedSheetNames) {
        String safeName = WorkbookUtil.createSafeSheetName(requestedName);
        String candidate = safeName;
        int suffix = 2;
        while (!usedSheetNames.add(candidate.toLowerCase(Locale.ROOT))) {
            String suffixText = " (" + suffix++ + ")";
            candidate = safeName.substring(0, Math.min(safeName.length(), 31 - suffixText.length())) + suffixText;
        }
        return candidate;
    }

    private record ParticipantRow(
            String eventName,
            String registrationCode,
            String registrationType,
            String participantRole,
            String firstName,
            String middleName,
            String lastName,
            String email,
            String phone,
            String college,
            String course,
            LocalDateTime registeredAt,
            String registrationStatus,
            String paymentStatus,
            BigDecimal amountDue) {
    }
}
