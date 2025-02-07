package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.parser.request.ParsingTask;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.entity.Student;
import com.techstud.scheduleuniversity.exception.ParserException;
import com.techstud.scheduleuniversity.exception.ParserResponseTimeoutException;

public interface ParserService {

    ScheduleParserResponse parseSchedule(ParsingTask parsingTask, Student student) throws ParserException, ParserResponseTimeoutException;

}
