package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.Group;
import com.techstud.scheduleuniversity.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUsername(String username);

    List<Student> findByGroup(Group group);
}
