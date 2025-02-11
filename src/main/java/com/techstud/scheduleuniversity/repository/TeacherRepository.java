package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.Teacher;
import com.techstud.scheduleuniversity.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByUniversityAndTeacherNameAndLastNameAndFirstNameAndMiddleName(
            University university, String teacherName, String lastName, String firstName, String middleName);
}
