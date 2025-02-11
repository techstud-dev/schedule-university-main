package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.entity.Group;
import com.techstud.scheduleuniversity.entity.University;
import com.techstud.scheduleuniversity.repository.GroupRepository;
import com.techstud.scheduleuniversity.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TransientObjectException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;

    @Override
    @Transactional
    public Group saveOrUpdate(Group group) {

        if (group.getUniversity().getId() == null) {
            throw new TransientObjectException("University in group: " + group + "out of context or not exists in db");
        }

        groupRepository
                .findByUniversityGroupIdAndGroupCode(group.getUniversityGroupId(), group.getGroupCode())
                .ifPresent(foundedGroup -> group.setId(foundedGroup.getId()));

        return groupRepository.save(group);
    }

    @Override
    @Transactional
    public List<Group> saveOrUpdateAll(List<Group> groups) {
        Set<Group> result = new HashSet<>();
        groups.forEach(group -> {
            result.add(saveOrUpdate(group));
        });
        return result.stream().toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Group findByUniversityAndGroupCode(String groupCode, University university) {
        return groupRepository.findByUniversityAndGroupCode(university, groupCode).orElse(null);
    }
}
