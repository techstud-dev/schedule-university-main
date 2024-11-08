package com.techstud.scheduleuniversity.entity.schedule;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "place",
        uniqueConstraints = @UniqueConstraint(columnNames = {"university_id", "name"})
)
@Data
@NoArgsConstructor
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "place_id_seq")
    @SequenceGenerator(name = "place_id_seq", sequenceName = "place_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @Column(name = "name", nullable = false)
    private String name;

    @Version
    private Long version;
}
