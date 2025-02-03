package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Long, Teacher> {

}
