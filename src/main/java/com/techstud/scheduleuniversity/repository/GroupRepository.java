package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Long, Group> {

}
