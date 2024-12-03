package com.techstud.scheduleuniversity.dto.fetcher.api.response;

public record SseuApiGroupDataResponse(Long id, String status, String name, String course, String direction,
                                       String directionCode, String faculty, String formOfTraining, String groupCode,
                                       String numberOfStudents, String program, String trainingPeriod,
                                       Long scheduleType, String typesEducation, String courseNum, String semester) {
}
