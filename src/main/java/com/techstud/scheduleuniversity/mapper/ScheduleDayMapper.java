package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDayDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleObjectDocument;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleDayParserResponse;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleObjectParserResponse;
import com.techstud.scheduleuniversity.dto.parser.response.TimeSheetParserResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleDayApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
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

    private Date fromStringToDate(String inputStringDate) throws ParseException {
        log.info("Input date format: {}", inputStringDate);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Define the format
        Date date = formatter.parse(inputStringDate);
        log.error("Invalid date format: {}", inputStringDate);
        log.info("Date format: {}", formatter.format(date));
        return date;
    }
}
