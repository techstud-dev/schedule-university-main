package com.techstud.scheduleuniversity.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "university_group", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "university_group_id",
                "group_code"})
})
@Data
@NoArgsConstructor
public class Group extends AuditableEntity {

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

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_schedule_id", referencedColumnName = "id")
    private Schedule groupSchedule;

    public Group(University university, String groupCode, String universityGroupId) {
        this.university = university;
        this.groupCode = groupCode;
        this.universityGroupId = universityGroupId;
    }
}
