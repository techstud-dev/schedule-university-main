package com.techstud.scheduleuniversity.entity.schedule;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "schedule")
@NoArgsConstructor
@Data
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule_id_seq")
    @SequenceGenerator(name = "schedule_id_seq", sequenceName = "schedule_id_seq", allocationSize = 1)
    private Long id;

    private LocalDate created;
    private LocalDate modified;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "day_of_week")
    @JoinColumn(name = "schedule_id")
    private Map<DayOfWeek, ScheduleDay> evenWeekSchedule;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "day_of_week")
    @JoinColumn(name = "schedule_id")
    private Map<DayOfWeek, ScheduleDay> oddWeekSchedule;

    private LocalDate snapshotDate;

    @Version
    private Long version;
}
