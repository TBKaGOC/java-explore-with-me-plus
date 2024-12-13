package ru.practicum.mapper;

import ru.practicum.model.Requests;
import ru.practicum.model.Response;

// TODO
import ru.practicum.StatsRequestDto;
import ru.practicum.StatsResponseDto;

public class Mapper {
    public static StatsRequestDto toRequestDto(Requests request) {
        StatsRequestDto statsDto = new StatsRequestDto();
        statsDto.setIp(request.getIp());
        statsDto.setApplication(request.getApplication());
        statsDto.setUri(request.getUri());
        statsDto.setMoment(request.getMoment());
        return statsDto;
    }

    public static Requests toRequest(StatsRequestDto requestDto) {
        Requests request = new Requests();
        request.setIp(requestDto.getIp());
        request.setApplication(requestDto.getApplication());
        request.setUri(requestDto.getUri());
        request.setMoment(requestDto.getMoment());
        return request;
    }

    public static StatsResponseDto toResponseDto(Response stats) {
        StatsResponseDto statsDto = new StatsResponseDto();
        statsDto.setApp(stats.getApplication());
        statsDto.setTotal(stats.getTotal());
        statsDto.setUri(stats.getUri());
        return statsDto;
    }
}
