package com.techstud.scheduleuniversity.repository.jpa;

import com.techstud.scheduleuniversity.dao.entity.UniversityGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UniversityGroupRepository extends JpaRepository<UniversityGroup, Long> {

    @Query(value =
            "SELECT ug.* FROM university_group ug " +
                                        "JOIN university u ON ug.university_id = u.id " +
            "WHERE u.shortName = :universityName AND ug.groupCode = :groupCode")
    UniversityGroup findByGroupCodeAndUniversityName(String groupCode, String universityName);

}
