package com.example.event_mgm.ServiceImpl.event;
import com.example.event_mgm.Repository.CategoryRepo;
import com.example.event_mgm.Repository.EventRepo;
import com.example.event_mgm.Repository.EventTypeRepo;
import com.example.event_mgm.dto.CreateEventRequestDto;
import com.example.event_mgm.dto.GetEventResponseDto;
import com.example.event_mgm.exceptions.InvalidData;
import com.example.event_mgm.exceptions.ResourceNotFoundException;
import com.example.event_mgm.model.Category;
import com.example.event_mgm.model.Event;
import com.example.event_mgm.model.EventType;
import com.example.event_mgm.service.event.EventService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
   private  final  EventRepo eventRepo;
   private  final EventTypeRepo eventTypeRepo;
   private  final CategoryRepo categoryRepo;
   private final ModelMapper  modelMapper;


    @Override
    public GetEventResponseDto createEvent(CreateEventRequestDto request) {

        // sanitize and validate data
        sanitizeRequest(request);

        Event event = selfRequestToEvent(request);

        Event savedEvent = eventRepo.save(event);

        return selfBuildResponse(savedEvent);
    }

    @Override
    public GetEventResponseDto getEventById(Long eventId) {
        return null;
    }

    @Override
    public List<GetEventResponseDto> getAllEvents() {
        return List.of();
    }

    @Override
    public GetEventResponseDto updateEvent(Long eventId, CreateEventRequestDto request) {
        return null;
    }

    @Override
    public void deleteEvent(Long eventId) {

    }

    @Override
    public void activateEvent(Long eventId) {

    }

    @Override
    public void deactivateEvent(Long eventId) {

    }

    // helper method to build response
    private GetEventResponseDto selfBuildResponse(Event event)
    {

        return  modelMapper.map(event,GetEventResponseDto.class);

    }

    //helper method to sanitize data
    private String sanitizeText(String value)
    {
        return value.trim();
    }

    private void sanitizeRequest(CreateEventRequestDto requestDto)
    {
        if((requestDto.getRegistrationDueDate().isAfter(requestDto.getEventDate())))
        {
            throw  new InvalidData("Invalid Event Date OR Invalid Event Due Date");
        }
        requestDto.setEventType(requestDto.getEventType().toUpperCase());

        if(requestDto.getEventType().equals("SOLO") && requestDto.getMaxParticipantPerGroup()>1)
        {
            throw  new InvalidData("Solo Type Event Must Contains 1 student per group");
        }

        requestDto.setEventName(sanitizeText(requestDto.getEventName()));
        requestDto.setDescription(sanitizeText(requestDto.getDescription()));
        requestDto.setVenue(sanitizeText(requestDto.getVenue()));

    }

    //helper method
    private  Event selfRequestToEvent(CreateEventRequestDto request)
    {

        EventType eventType = eventTypeRepo.findByEventType(request.getEventType()
                .toUpperCase()).orElseThrow(()->new ResourceNotFoundException("Please Enter Valid Event Name"));

        Category eventCategory = categoryRepo.findByName(request.getCategoryName().toUpperCase())
                .orElseThrow(()->new ResourceNotFoundException("Please Enter Valid Category"));
        Event event  = Event.builder()
                .eventDate(request.getEventDate())
                .eventName(request.getEventName())

                .eventType(eventType)
                .category(eventCategory)
                .description(request.getDescription())
                .venue(request.getVenue())
                .maxParticipants(request.getMaxParticipants())
                .registrationDueDate(request.getRegistrationDueDate())
                .maxParticipantPerGroup(request.getMaxParticipantPerGroup())
                .build();
        return  event;
    }

}
