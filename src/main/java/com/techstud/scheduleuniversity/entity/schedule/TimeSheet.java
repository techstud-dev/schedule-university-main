package com.techstud.scheduleuniversity.entity.schedule;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(
        name = "time_sheet",
        uniqueConstraints = @UniqueConstraint(columnNames = {"university_id", "time_from", "time_to"})
)
@NoArgsConstructor
@Data
public class TimeSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "time_sheet_id_seq")
    @SequenceGenerator(name = "time_sheet_id_seq", sequenceName = "time_sheet_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "university_id")
    private University university;

    @Column(name = "time_from")
    private LocalTime timeFrom;

    @Column(name = "time_to")
    private LocalTime timeTo;

    @Version
    private Long version;

}
