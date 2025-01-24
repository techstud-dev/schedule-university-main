package com.techstud.scheduleuniversity.repository.mongo;

import com.techstud.scheduleuniversity.dao.document.schedule.TimeSheetDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface TimeSheetRepository extends MongoRepository<TimeSheetDocument, String> {

    Optional<TimeSheetDocument> findByFromAndTo(LocalTime from, LocalTime to);
}
