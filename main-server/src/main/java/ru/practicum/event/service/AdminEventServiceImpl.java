package ru.practicum.event.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryMapper;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.client.StatsClient;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.model.EventRequest;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.dto.UserMapper;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminEventServiceImpl implements AdminEventService {
    final EventRepository eventRepository;
    final RequestRepository requestRepository;
    final UserRepository userRepository;
    final CategoryRepository categoryRepository;

    final StatsClient statsClient;

    @Override
    public List<EventFullDto> getEvents(List<Long> users, List<String> states, List<Long> categories, String rangeStart, String rangeEnd, Integer from, Integer size) {

        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        List<EventFullDto> eventDtos = null;
        List<EventState> eventStateList = null;

        if (rangeStart != null) {
            startDateTime = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (rangeEnd != null) {
            endDateTime = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (startDateTime.isAfter(endDateTime)) {
                throw new ValidationException("Время начала поиска позже времени конца поиска");
            }
        }

        if ((users == null) || (users.isEmpty())) {
            users = userRepository.findAllId();
        }
        if ((categories == null) || (categories.isEmpty())) {
            categories = categoryRepository.findAllId();
        }
        if ((states == null) || (states.isEmpty())) {
            eventStateList = Arrays.stream(EventState.values()).collect(Collectors.toList());
        } else {
            eventStateList = states.stream().map(EventState::valueOf).collect(Collectors.toList());
        }

        List<Event> allEventsWithDates = new ArrayList<>(eventRepository.findAllEventsWithDates(users,
                eventStateList, categories, startDateTime, endDateTime, PageRequest.of(from / size, size)));
        List<EventRequest> requestsByEventIds = requestRepository.findByEventIds(allEventsWithDates.stream()
                .mapToLong(Event::getId).boxed().collect(Collectors.toList()));
        eventDtos = allEventsWithDates.stream()
                .map(e -> EventMapper.mapEventToFullDto(e,
                        requestsByEventIds.stream()
                                .filter(r -> r.getEvent().getId().equals(e.getId()))
                                .count()))
                .toList();

        if (!eventDtos.isEmpty()) {
            HashMap<Long, Integer> eventIdsWithViewsCounter = new HashMap<>();
            LocalDateTime startTime = LocalDateTime.parse(eventDtos.getFirst().getCreatedOn().replace(" ", "T"));
            ArrayList<String> uris = new ArrayList<>();
            for (EventFullDto dto : eventDtos) {
                eventIdsWithViewsCounter.put(dto.getId(), 0);
                uris.add("/events/" + dto.getId().toString());
                if (startTime.isAfter(LocalDateTime.parse(dto.getCreatedOn().replace(" ", "T")))) {
                    startTime = LocalDateTime.parse(dto.getCreatedOn().replace(" ", "T"));
                }
            }

            var viewsCounter = statsClient.getAllStats(startTime, LocalDateTime.now(), uris, true);
            for (var statsDto : viewsCounter) {
                String[] split = statsDto.getUri().split("/");
                eventIdsWithViewsCounter.put(Long.parseLong(split[2]), Math.toIntExact(statsDto.getHits()));
            }
            ArrayList<Long> longs = new ArrayList<>(eventIdsWithViewsCounter.keySet());
            List<EventRequest> requests = requestRepository.findByEventIdsAndStatus(longs, "CONFIRMED");
            return eventDtos.stream()
                    .peek(dto -> dto.setConfirmedRequests(
                            requests.stream()
                                    .filter((request -> request.getEvent().getId().equals(dto.getId())))
                                    .count()
                    ))
                    .peek(dto -> dto.setViews(eventIdsWithViewsCounter.get(dto.getId())))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public EventFullDto updateEvent(Long eventId, EventFullDto event) {
        Event oldEvent = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Искомое событие не найдено " + eventId)
        );

        if (event.getState() != null &&
                event.getState().equals("PUBLISHED") &&
                oldEvent.getState() != EventState.PENDING) {
            throw new ConflictException("Невозможно опубликовать событие " + eventId);
        }

        if (event.getState() != null &&
                event.getState().equals("CANCELED") &&
                oldEvent.getState() != EventState.PENDING) {
            throw new ConflictException("Невозможно отменить событие " + eventId);
        }
        if (ChronoUnit.HOURS.between(oldEvent.getPublishedOn(), oldEvent.getEventDate()) < 1) {
            throw new ConflictException("Невозможно изменить событие" + eventId);
        }

        Event newEvent = new Event();
        newEvent.setId(eventId);
        newEvent.setAnnotation(event.getAnnotation() != null ? event.getAnnotation() : oldEvent.getAnnotation());
        newEvent.setCategory(event.getCategory() != null ?
                CategoryMapper.mapCategoryDto(event.getCategory()) : oldEvent.getCategory());
        newEvent.setCreatedOn(event.getCreatedOn() != null ?
                LocalDateTime.parse(event.getCreatedOn()) : oldEvent.getCreatedOn());
        newEvent.setDescription(event.getDescription() != null ? event.getDescription() : oldEvent.getDescription());
        newEvent.setEventDate(event.getEventDate() != null ? event.getEventDate() : oldEvent.getEventDate());
        newEvent.setInitiator(event.getInitiator() != null ?
                UserMapper.mapUserDto(event.getInitiator()) : oldEvent.getInitiator());
        newEvent.setLocation(event.getLocation() != null ? event.getLocation() : oldEvent.getLocation());
        newEvent.setPaid(event.getPaid() != null ? event.getPaid() : oldEvent.getPaid());
        newEvent.setParticipantLimit(event.getParticipantLimit() != null ?
                event.getParticipantLimit() : oldEvent.getParticipantLimit());
        newEvent.setPublishedOn(event.getPublishedOn() != null ?
                LocalDateTime.parse(event.getPublishedOn()) : oldEvent.getPublishedOn());
        newEvent.setRequestModeration(event.getRequestModeration() != null ?
                event.getRequestModeration() : oldEvent.getRequestModeration());
        newEvent.setState(event.getState() != null ? EventState.valueOf(event.getState()) : oldEvent.getState());
        newEvent.setTitle(event.getTitle() != null ? event.getTitle() : oldEvent.getTitle());

        eventRepository.saveLocation(newEvent.getLocation().getLat(), newEvent.getLocation().getLon());

        return EventMapper.mapEventToFullDto(eventRepository.save(newEvent), event.getConfirmedRequests());
    }
}