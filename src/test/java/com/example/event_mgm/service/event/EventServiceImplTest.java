package com.example.event_mgm.service.event;

import com.example.event_mgm.Repository.admin.CategoryRepository;
import com.example.event_mgm.Repository.admin.EventTypeRepository;
import com.example.event_mgm.Repository.event.EventRepository;
import com.example.event_mgm.Repository.role.UserRepository;
import com.example.event_mgm.ServiceImpl.event.EventServiceImpl;
import com.example.event_mgm.exceptions.ResourceNotFoundException;
import com.example.event_mgm.model.Event;
import com.example.event_mgm.model.Registration;
import com.example.event_mgm.model.TeamMember;
import com.example.event_mgm.service.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EventServiceImplTest {

    private EventRepository eventRepository;
    private FileStorageService fileStorageService;
    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepository.class);
        fileStorageService = mock(FileStorageService.class);
        eventService = new EventServiceImpl(
                eventRepository,
                mock(EventTypeRepository.class),
                mock(CategoryRepository.class),
                mock(UserRepository.class),
                mock(ModelMapper.class),
                fileStorageService);
    }

    @Test
    void deletesEventWithRegistrationsAndCleansTheirPaymentProofs() {
        Registration registration = new Registration();
        registration.setPaymentProofPath("uploads/payments/payment-1.png");
        registration.setTeamMembers(List.of(new TeamMember()));
        Event event = new Event();
        event.setId(42L);
        event.setRegistrations(List.of(registration));
        when(eventRepository.findById(42L)).thenReturn(Optional.of(event));

        eventService.deleteEvent(42L);

        verify(eventRepository).delete(event);
        verify(fileStorageService).deleteFile("uploads/payments/payment-1.png");
    }

    @Test
    void throwsWhenEventDoesNotExist() {
        when(eventRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.deleteEvent(404L));

        verifyNoInteractions(fileStorageService);
    }
}
