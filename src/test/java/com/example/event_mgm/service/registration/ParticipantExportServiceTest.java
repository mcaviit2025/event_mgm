package com.example.event_mgm.service.registration;

import com.example.event_mgm.Repository.event.EventRepository;
import com.example.event_mgm.Repository.repository.RegistrationRepository;
import com.example.event_mgm.Repository.repository.TeamMemberRepository;
import com.example.event_mgm.enums.PaymentStatus;
import com.example.event_mgm.enums.RegistrationStatus;
import com.example.event_mgm.model.Event;
import com.example.event_mgm.model.Registration;
import com.example.event_mgm.model.TeamMember;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ParticipantExportServiceTest {

    private EventRepository eventRepository;
    private RegistrationRepository registrationRepository;
    private TeamMemberRepository teamMemberRepository;
    private ParticipantExportService participantExportService;
    private Event event;
    private Registration registration;
    private TeamMember teammate;

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepository.class);
        registrationRepository = mock(RegistrationRepository.class);
        teamMemberRepository = mock(TeamMemberRepository.class);
        participantExportService = new ParticipantExportService(
                eventRepository, registrationRepository, teamMemberRepository);

        event = new Event();
        event.setId(7L);
        event.setEventName("Build/Design Challenge");
        event.setEventDateTime(LocalDateTime.of(2026, 10, 30, 10, 0));

        registration = new Registration();
        registration.setId(91L);
        registration.setEvent(event);
        registration.setUniqueCode("REG-91");
        registration.setRegistrationType("GROUP");
        registration.setFirstName("Alex");
        registration.setLastName("Leader");
        registration.setEmail("alex@example.test");
        registration.setPhoneNumber("1234567890");
        registration.setCollegeName("Example College");
        registration.setCourse("Computer Science");
        registration.setRegisteredAt(LocalDateTime.of(2026, 10, 20, 12, 0));
        registration.setRegistrationStatus(RegistrationStatus.APPROVED.name());
        registration.setPaymentStatus(PaymentStatus.VERIFIED.name());
        registration.setAmountDue(new BigDecimal("125.50"));

        teammate = new TeamMember();
        teammate.setId(92L);
        teammate.setRegistration(registration);
        teammate.setFirstName("Sam");
        teammate.setLastName("Member");
        teammate.setEmail("sam@example.test");
        teammate.setPhoneNumber("0987654321");

        when(eventRepository.findByIdInAndIsActiveTrueOrderByEventNameAsc(List.of(7L)))
                .thenReturn(List.of(event));
        when(registrationRepository
                .findByEventIdInAndIsActiveTrueOrderByEventIdAscRegisteredAtAsc(List.of(7L)))
                .thenReturn(List.of(registration));
        when(teamMemberRepository
                .findByRegistrationIdInAndIsActiveTrueOrderByRegistrationIdAscIdAsc(List.of(91L)))
                .thenReturn(List.of(teammate));
    }

    @Test
    void exportsGroupLeaderAndTeammateWithSeparateEventAndCombinedSheets() throws Exception {
        byte[] content = participantExportService.export(List.of(7L), null, null, true);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            var combined = workbook.getSheet("All Participants");
            assertEquals(2, combined.getLastRowNum());
            assertEquals("Leader", combined.getRow(1).getCell(3).getStringCellValue());
            assertEquals("alex@example.test", combined.getRow(1).getCell(7).getStringCellValue());
            assertEquals(125.5, combined.getRow(1).getCell(14).getNumericCellValue());
            assertEquals("Team member", combined.getRow(2).getCell(3).getStringCellValue());
            assertEquals("sam@example.test", combined.getRow(2).getCell(7).getStringCellValue());
            assertNull(combined.getRow(2).getCell(14));
            assertEquals(2, workbook.getSheetAt(2).getLastRowNum());
            assertEquals("Build/Design Challenge", workbook.getSheet("Summary")
                    .getRow(1).getCell(0).getStringCellValue());
        }
        verify(teamMemberRepository)
                .findByRegistrationIdInAndIsActiveTrueOrderByRegistrationIdAscIdAsc(List.of(91L));
    }

    @Test
    void filtersRegistrationsAndCanExcludeTeamMembers() throws Exception {
        byte[] content = participantExportService.export(
                List.of(7L), PaymentStatus.VERIFIED, RegistrationStatus.APPROVED, false);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            assertEquals(1, workbook.getSheet("All Participants").getLastRowNum());
            assertEquals("Leader", workbook.getSheet("All Participants")
                    .getRow(1).getCell(3).getStringCellValue());
        }
        verify(registrationRepository)
                .findByEventIdInAndIsActiveTrueOrderByEventIdAscRegisteredAtAsc(eq(List.of(7L)));
        verifyNoInteractions(teamMemberRepository);
    }
}
