package com.techstud.scheduleuniversity.dao.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "university_group", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"university_id", "university_group_id", "group_code"})
})
@Data
@NoArgsConstructor
public class UniversityGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "university_group_id_seq", sequenceName = "university_group_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id")
    @Fetch(FetchMode.JOIN)
    private University university;
    private String universityGroupId;
    private String groupCode;
    private String scheduleMonoDbId;

}
