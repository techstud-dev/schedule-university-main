package com.techstud.scheduleuniversity.entity.schedule;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "schedule_object")
@NoArgsConstructor
@Data
public class ScheduleObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule_object_id_seq")
    @SequenceGenerator(name = "schedule_object_id_seq", sequenceName = "schedule_object_id_seq", allocationSize = 1)
    private Long id;

    private LocalDate created;
    private LocalDate modified;

    @Enumerated(EnumType.STRING)
    private ScheduleType type;

    private String name;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToMany
    @JoinTable(
            name = "schedule_object_group",
            joinColumns = @JoinColumn(name = "schedule_object_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private List<Group> groups;
}
