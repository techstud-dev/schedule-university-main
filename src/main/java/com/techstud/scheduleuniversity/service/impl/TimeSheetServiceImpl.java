package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.entity.TimeSheet;
import com.techstud.scheduleuniversity.repository.TimeSheetRepository;
import com.techstud.scheduleuniversity.service.TimeSheetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
