package com.techstud.scheduleuniversity.repository.mongo;

import com.techstud.scheduleuniversity.dao.document.schedule.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRepository extends MongoRepository<Schedule, String> {

}
