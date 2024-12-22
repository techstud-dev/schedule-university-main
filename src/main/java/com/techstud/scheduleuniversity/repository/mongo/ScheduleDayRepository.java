package com.techstud.scheduleuniversity.repository.mongo;

import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDayDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface ScheduleDayRepository extends MongoRepository<ScheduleDayDocument, String> {

    Optional<ScheduleDayDocument> findByDate(Date date);
}
