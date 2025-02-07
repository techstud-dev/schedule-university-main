package com.techstud.scheduleuniversity.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
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
