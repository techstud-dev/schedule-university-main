package com.techstud.scheduleuniversity.repository;

import com.techstud.scheduleuniversity.entity.TimeSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeSheetRepository extends JpaRepository<TimeSheet, Long> {

}
