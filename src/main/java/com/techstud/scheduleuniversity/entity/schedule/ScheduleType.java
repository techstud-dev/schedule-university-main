package com.techstud.scheduleuniversity.entity.schedule;

import lombok.Getter;

@Getter
public enum ScheduleType {
    LECTURE,
    PRACTICE,
    LAB,
    EXAM,
    CONSULTATION,
    INDEPENDENT_WORK,
    UNKNOWN;
}
