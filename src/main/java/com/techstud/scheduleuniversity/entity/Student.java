package com.techstud.scheduleuniversity.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "students", indexes = {
        @Index(name = "student_username_index", columnList = "username")
})
public class Student extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "student_id_seq", sequenceName = "student_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "username", unique = true)
    private String username;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule personalSchedule;

}
