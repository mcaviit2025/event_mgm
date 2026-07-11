package com.example.event_mgm.ServiceImpl.event;

import com.example.event_mgm.Repository.CategoryRepo;
import com.example.event_mgm.Repository.EventRepo;
import com.example.event_mgm.Repository.EventTypeRepo;
import com.example.event_mgm.dto.CreateEventRequestDto;
import com.example.event_mgm.dto.GetEventResponseDto;
import com.example.event_mgm.exceptions.ResourceNotFoundException;
import com.example.event_mgm.model.Category;
import com.example.event_mgm.model.EventType;
import com.example.event_mgm.service.event.EventService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {
   private  final  EventRepo eventRepo;
   private  final EventTypeRepo eventTypeRepo;
   private  final CategoryRepo categoryRepo;

    @Override
    public GetEventResponseDto createEvent(CreateEventRequestDto request) {

          sanitizeRequest(request);

           EventType eventType = eventTypeRepo.findByName(request.getEventType()
                  .toUpperCase()).orElseThrow(()->new ResourceNotFoundException("Please Enter Valid Event Name"));

           Category eventCategory = categoryRepo.findByName(request.getCategoryName().toUpperCase())
                .orElseThrow(()->new ResourceNotFoundException("Please Enter Valid Category"));








        return null;
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
    private GetEventResponseDto selfBuildResponse(CreateEventRequestDto requestDto)
    {

        return  null;

    }
    //helper method to sanitize data
    private String sanitizeText(String value)
    {
        return value.trim();
    }
    private CreateEventRequestDto sanitizeRequest(CreateEventRequestDto requestDto)
    {
        requestDto.setEventName(sanitizeText(requestDto.getEventName()));
        requestDto.setDescription(sanitizeText(requestDto.getDescription()));
        requestDto.setVenue(sanitizeText(requestDto.getVenue()));
        return requestDto;
    }

}
