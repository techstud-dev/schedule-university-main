package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.Group;
import com.techstud.scheduleuniversity.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Long, Group> {

    Optional<Group> findByUniversityAndGroupCode(University university, String groupCode);
}
