package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.entity.Schedule;
import com.techstud.scheduleuniversity.exception.ScheduleNotFoundException;
import com.techstud.scheduleuniversity.repository.ScheduleRepository;
import com.techstud.scheduleuniversity.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TransientObjectException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;

    @Override
    @Transactional
    public Schedule saveOrUpdate(Schedule schedule) {
        schedule.getLessonList().forEach(lesson -> {
            if (lesson.getId() == null) {
                throw new TransientObjectException("Lesson in schedule: " + schedule + "out of context or not exists in db");
            }

            if (lesson.getPlace().getId() == null) {
                throw new TransientObjectException("Place in lesson in schedule: " + schedule + "out of context or not exists in db");
            }

            if (lesson.getTeacher().getId() == null) {
                throw new TransientObjectException("Teacher in lesson in schedule: " + schedule + "out of context or not exists in db");
            }
        });

        scheduleRepository.findScheduleByLessonList(schedule.getLessonList())
                .ifPresent(foundedSchedule -> schedule.setId(foundedSchedule.getId()));
        return scheduleRepository.save(schedule);

    }

    @Override
    @Transactional
    public List<Schedule> saveOrUpdateAll(List<Schedule> schedules) {
        Set<Schedule> savedSchedules = new HashSet<>();
        schedules.forEach(schedule -> savedSchedules.add(saveOrUpdate(schedule)));
        return savedSchedules.stream().toList();
    }

    @Override
    public void deleteById(Long id) {
        scheduleRepository.deleteById(id);
    }

    @Override
    public Schedule findById(Long id) throws ScheduleNotFoundException {
       return scheduleRepository
               .findById(id)
               .orElseThrow(() -> new ScheduleNotFoundException("Schedule with id: " + id + " not found"));
    }
}
