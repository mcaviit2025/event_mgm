package com.example.event_mgm.controller;

import com.example.event_mgm.ServiceImpl.event.EventServiceImpl;
import com.example.event_mgm.dto.CreateEventRequestDto;
import com.example.event_mgm.dto.GetEventResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event")
public class EventController {
    private  final EventServiceImpl eventService;

    public EventController(EventServiceImpl eventService) {
        this.eventService = eventService;
    }


    @PostMapping("/createEvent")
    public ResponseEntity<GetEventResponseDto> createEvent(@Valid @RequestBody CreateEventRequestDto requestDto)
    {
        GetEventResponseDto response = eventService.createEvent(requestDto);
        return  new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
