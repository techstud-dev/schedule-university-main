package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.schedule.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Group findByGroupCodeAndUniversityId(String groupCode, Long universityId);
}