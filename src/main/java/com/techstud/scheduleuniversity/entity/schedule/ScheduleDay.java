package com.techstud.scheduleuniversity.entity.schedule;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "schedule_day")
@NoArgsConstructor
@Data
public class ScheduleDay {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule_day_id_seq")
    @SequenceGenerator(name = "schedule_day_id_seq", sequenceName = "schedule_day_id_seq", allocationSize = 1)
    private Long id;

    private LocalDate date;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyJoinColumn(name = "time_sheet_id")
    private Map<TimeSheet, ScheduleObjectMapping> lessons;
}
