package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.entity.TimeSheet;
import com.techstud.scheduleuniversity.repository.TimeSheetRepository;
import com.techstud.scheduleuniversity.service.TimeSheetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class TimeSheetServiceImpl implements TimeSheetService {

    private final TimeSheetRepository timeSheetRepository;
    @Override
    @Transactional
    public TimeSheet saveOrUpdate(TimeSheet timeSheet) {
        timeSheetRepository
                .findTimeSheetByFromTimeAndToTime(timeSheet.getFromTime(), timeSheet.getToTime())
                .ifPresent(foundedTimeSheet -> timeSheet.setId(foundedTimeSheet.getId()));

        return timeSheetRepository.save(timeSheet);
    }

    @Override
    @Transactional
    public List<TimeSheet> saveOrUpdateAll(List<TimeSheet> timeSheets) {
        Set<TimeSheet> timeSheetSet = new HashSet<>();
        timeSheets.forEach(timeSheet -> timeSheetSet.add(saveOrUpdate(timeSheet)));
        return timeSheetSet.stream().toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TimeSheet findByStandardPattern(String pattern) {
        LocalTime from = LocalTime.parse(pattern.split("-")[0]);
        LocalTime to = LocalTime.parse(pattern.split("-")[1]);
        return timeSheetRepository.findTimeSheetByFromTimeAndToTime(from, to)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSheet not found for time fromL " + from + ", to: " + to));
    }
}
