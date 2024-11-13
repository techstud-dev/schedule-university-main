package com.techstud.scheduleuniversity.dao.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "university", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"shortName", "fullName", "url", "mongoDbId"})
})
@Data
@NoArgsConstructor
public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "university_id_seq", sequenceName = "university_id_seq", allocationSize = 1)
    private Long id;

    private String fullName;
    private String shortName;
    private String url;

    private String mongoDbId;

    @Version
    private Long version;
}
