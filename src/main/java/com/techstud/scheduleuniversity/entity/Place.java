package com.techstud.scheduleuniversity.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "place", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "university_id",
                "place_name"
        })
})
@Data
@NoArgsConstructor
public class Place extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "place_id_seq", sequenceName = "place_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "place_name")
    private String placeName;

    @ManyToOne
    @JoinColumn(name = "university_id")
    private University university;

}
