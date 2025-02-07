package com.techstud.scheduleuniversity.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum LessonType {

    LECTURE("Лекция"),
    PRACTICE("Практика"),
    LAB("Лабораторная работа"),
    EXAM("Экзамен/зачет"),
    CONSULTATION("Консультация"),
    INDEPENDENT_WORK("Самостоятельная работа"),
    UNKNOWN("Другое");

    private final String ruName;

    public static LessonType ruValueOf(String ruValue) {
        for (LessonType scheduleType : values()) {
            if (scheduleType.ruName.equalsIgnoreCase(ruValue)) {
                return scheduleType;
            }
        }
        return UNKNOWN;
    }
}
