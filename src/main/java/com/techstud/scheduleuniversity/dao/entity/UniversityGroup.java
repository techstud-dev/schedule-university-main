package com.techstud.scheduleuniversity.dao.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "university_group", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"university_group_id", "group_code"})
})
@Data
@NoArgsConstructor
public class UniversityGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "university_group_id_seq", sequenceName = "university_group_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "university_id")
    private University university;

    @Column(name = "university_group_id")
    private String universityGroupId;

    @Column(name = "group_code")
    private String groupCode;

    @Column(name = "schedule_mongo_id")
    private String scheduleMongoId;

    public UniversityGroup(University university, String groupCode, String universityGroupId) {
        this.university = university;
        this.groupCode = groupCode;
        this.universityGroupId = universityGroupId;
    }
}
