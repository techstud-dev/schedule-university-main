package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.entity.University;
import com.techstud.scheduleuniversity.repository.UniversityRepository;
import com.techstud.scheduleuniversity.service.UniversityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UniversityServiceImpl implements UniversityService {

    private final UniversityRepository universityRepository;

    @Override
    @Transactional(readOnly = true)
    public University findByShortName(String shortName) {
        return universityRepository.findByShortName(shortName)
                .orElseThrow(() -> new ResourceNotFoundException("University with short name " + shortName + " not found"));
    }
}
