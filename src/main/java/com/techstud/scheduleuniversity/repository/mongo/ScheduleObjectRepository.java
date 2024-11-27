package com.techstud.scheduleuniversity.repository.mongo;

import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleObject;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleObjectRepository extends MongoRepository<ScheduleObject, String> {

}
