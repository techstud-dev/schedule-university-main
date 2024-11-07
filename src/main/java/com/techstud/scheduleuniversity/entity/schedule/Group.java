package com.techstud.scheduleuniversity.entity.schedule;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "schedule_group",
        uniqueConstraints = @UniqueConstraint(columnNames = {"university_id", "group_code"})
)
@NoArgsConstructor
@Data
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule_group_id_seq")
    @SequenceGenerator(name = "schedule_group_id_seq", sequenceName = "schedule_group_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "university_id")
    private University university;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "group_code", nullable = false, unique = true)
    private String groupCode;

    @Version
    private Long version;
}
