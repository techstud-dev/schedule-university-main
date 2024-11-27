package com.techstud.scheduleuniversity.dao.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "students")
@NoArgsConstructor
@Data
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "student_id_seq", sequenceName = "student_id_seq", allocationSize = 1)
    private Long id;

    @Column(unique = true, name = "username")
    private String username;

    @Column(name = "last_action")
    private LocalDate lastAction = LocalDate.now();

    @Column(name = "schedule_mongo_id")
    private String scheduleMongoId;

    public Student(String username) {
        this.username = username;
    }
}
