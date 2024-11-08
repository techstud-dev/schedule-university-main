package com.techstud.scheduleuniversity.entity.schedule;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "schedule_object_mapping")
@Data
public class ScheduleObjectMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule_object_mapping_id_seq")
    @SequenceGenerator(name = "schedule_object_mapping_id_seq", sequenceName = "schedule_object_mapping_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "time_sheet_id")
    private TimeSheet timeSheet;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleObject> scheduleObjects;

    @Version
    private Long version;

    public ScheduleObjectMapping(TimeSheet timeSheet, List<ScheduleObject> scheduleObjects) {
        this.timeSheet = timeSheet;
        this.scheduleObjects = scheduleObjects;
    }

    public ScheduleObjectMapping() {

    }
}
