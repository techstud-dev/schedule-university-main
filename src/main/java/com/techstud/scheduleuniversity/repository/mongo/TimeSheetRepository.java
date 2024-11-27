package com.techstud.scheduleuniversity.repository.mongo;

import com.techstud.scheduleuniversity.dao.document.schedule.TimeSheet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeSheetRepository extends MongoRepository<TimeSheet, String> {

}
