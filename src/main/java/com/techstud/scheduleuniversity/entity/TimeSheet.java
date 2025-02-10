package com.techstud.scheduleuniversity.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@EqualsAndHashCode(callSuper = false, of = {"fromTime", "toTime"})
@Entity
@Table(name = "time_sheet", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "from_time",
                "to_time"})
}
)
@NoArgsConstructor
@Data
public class TimeSheet extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "time_sheet_id_seq", sequenceName = "time_sheet_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "from_time", columnDefinition = "TIME")
    private LocalTime fromTime;

    @Column(name = "to_time", columnDefinition = "TIME")
    private LocalTime toTime;

}
