package com.techstud.scheduleuniversity.entity.schedule;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "university", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "full_name", "link"}))
@NoArgsConstructor
@Data
public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "university_id_seq")
    @SequenceGenerator(name = "university_id_seq", sequenceName = "university_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "link", nullable = false)
    private String link;

}
