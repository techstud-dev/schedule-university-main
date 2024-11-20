package com.techstud.scheduleuniversity.repository.jpa;

import com.techstud.scheduleuniversity.dao.entity.UniversityGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UniversityGroupRepository extends JpaRepository<UniversityGroup, Long> {

    @Query("SELECT ug FROM UniversityGroup ug " +
            "JOIN FETCH ug.university u " +
            "WHERE u.shortName = :universityName AND ug.groupCode = :groupCode")
    @Transactional
    UniversityGroup findByGroupCodeAndUniversityName(@Param("groupCode") String groupCode,
                                                     @Param("universityName") String universityName);



}
