package ru.practicum.request.service;

import ru.practicum.request.dto.EventRequestDto;

import java.util.List;

public interface EventRequestService {

    EventRequestDto addRequest(Long userId, Long eventId);

    List<EventRequestDto> getUserRequests(Long userId);

    List<EventRequestDto> getRequestsByEventId(Long userId, Long eventId);

    EventRequestDto updateRequest(Long userId,
                                  Long eventId,
                                  EventRequestDto request);

    EventRequestDto cancelRequest(Long userId, Long requestId);
}
