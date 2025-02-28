package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.CreateScheduleDto;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.UpdateScheduleRequest;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.dto.response.schedule.ScheduleApiResponse;
import com.techstud.scheduleuniversity.exception.*;
import org.springframework.hateoas.EntityModel;

public interface ScheduleServiceFacade {

    EntityModel<ScheduleApiResponse> importSchedule(ImportDto importDto, String username)
            throws ResourceNotFoundException, ParserException, ParserResponseTimeoutException, RequestException;

    EntityModel<ScheduleApiResponse> forceImportSchedule(ImportDto importDto, String username)
            throws ParserException, RequestException, ParserResponseTimeoutException;

    EntityModel<ScheduleApiResponse> createSchedule(CreateScheduleDto saveDto, String username) throws ResourceExistsException;

    void deleteSchedule(Long id, String username) throws ScheduleNotFoundException;

    EntityModel<ScheduleApiResponse> getScheduleById(Long scheduleId) throws ScheduleNotFoundException;

    EntityModel<ScheduleApiResponse> getScheduleByStudent(String username) throws ParserException, ParserResponseTimeoutException;

    EntityModel<ScheduleApiResponse> updateSchedule(ApiRequest<UpdateScheduleRequest> request, String name, Long scheduleId);
}


