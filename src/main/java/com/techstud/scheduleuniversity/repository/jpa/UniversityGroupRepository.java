package com.techstud.scheduleuniversity.repository.jpa;

import com.techstud.scheduleuniversity.dao.entity.UniversityGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UniversityGroupRepository extends JpaRepository<UniversityGroup, Long> {

    UniversityGroup findByUniversity_ShortNameAndGroupCode(String universityName, String groupCode);



}
