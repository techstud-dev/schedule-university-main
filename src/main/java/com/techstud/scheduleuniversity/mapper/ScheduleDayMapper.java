package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDayDocument;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDayApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ScheduleDayMapper {

    private final ScheduleObjectMapper objectMapper = new ScheduleObjectMapper();

    public ScheduleDayApiResponse toResponse(ScheduleDayDocument scheduleDayDocument){
        ScheduleDayApiResponse response = new ScheduleDayApiResponse();
        response.setDate(scheduleDayDocument.getDate().toString());
        response.setLessons(scheduleDayDocument.getLessons()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(objectMapper::toApiResponse)
                                .collect(Collectors.toList())
                ))
        );
        return response;
    }

    public ScheduleDayDocument toDocument(ScheduleDayApiResponse scheduleDayApiResponse) throws ParseException {
        ScheduleDayDocument scheduleDayDocument = new ScheduleDayDocument();
        scheduleDayDocument.setDate(fromStringToDate(scheduleDayApiResponse.getDate()));
        scheduleDayDocument.setLessons(scheduleDayApiResponse.getLessons().entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(objectMapper::fromApiResponse)
                                .collect(Collectors.toList())
                )));
        return scheduleDayDocument;
    }

    private Date fromStringToDate(String inputStringDate) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); // Define the format
        Date date = formatter.parse(inputStringDate);
        log.error("Invalid date format: {}", inputStringDate);
        return date;
    }
}
