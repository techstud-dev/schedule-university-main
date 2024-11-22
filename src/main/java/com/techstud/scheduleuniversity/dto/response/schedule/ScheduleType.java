package com.techstud.scheduleuniversity.dto.response.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor(force = true)
@Getter
public enum ScheduleType implements Serializable {

    LECTURE("Лекция"),
    PRACTICE("Практика"),
    LAB("Лабораторная работа"),
    EXAM("Экзамен/зачет"),
    CONSULTATION("Консультация"),
    INDEPENDENT_WORK("Самостоятельная работа"),
    UNKNOWN("Другое");

    private final String ruName;
}
