package com.techstud.scheduleuniversity.entity.homework;

import com.techstud.scheduleuniversity.entity.schedule.ScheduleObject;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Data
public class HomeWork {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "homework_sequence", sequenceName = "homework_sequence", allocationSize = 1)
    private Long id;

    private LocalDate deadline;

    private String name;

    private String description;

    @ManyToOne
    @JoinColumn(name = "target_schedule_object_id", referencedColumnName = "id")
    private ScheduleObject targetScheduleObject;

    @Version
    private Long version;
}
