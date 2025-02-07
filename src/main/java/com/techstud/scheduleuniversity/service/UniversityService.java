package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.entity.University;

public interface UniversityService {
    University findByShortName(String shortName);


}
