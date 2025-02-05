package com.techstud.scheduleuniversity.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "lessons", uniqueConstraints = @UniqueConstraint(columnNames = {
        "is_even_week", "time_sheet_id", "name", "day_of_week", "type", "teacher_id", "place_id"
}))
@NoArgsConstructor
@Data
public class Lesson extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "lesson_id_seq", sequenceName = "lesson_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "is_even_week", nullable = false)
    private boolean evenWeek;

    @ManyToOne
    @JoinColumn(name = "time_sheet_id", nullable = false)
    private TimeSheet timeSheet;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LessonType type;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToMany
    @JoinTable(
            name = "lesson_group",
            joinColumns = @JoinColumn(name = "lesson_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private List<Group> groups;

}
