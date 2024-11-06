package com.techstud.scheduleuniversity.entity.schedule;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "teacher")
@NoArgsConstructor
@Data
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "teacher_id_seq")
    @SequenceGenerator(name = "teacher_id_seq", sequenceName = "teacher_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "university_id")
    private University university;

    private String name;

    @Version
    private Long version;
}
