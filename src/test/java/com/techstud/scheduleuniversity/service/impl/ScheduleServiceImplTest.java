package com.techstud.scheduleuniversity.service.impl;

import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dao.entity.Student;
import com.techstud.scheduleuniversity.dao.entity.University;
import com.techstud.scheduleuniversity.dao.entity.UniversityGroup;
import com.techstud.scheduleuniversity.exception.ScheduleNotFoundException;
import com.techstud.scheduleuniversity.exception.StudentNotFoundException;
import com.techstud.scheduleuniversity.kafka.KafkaMessageObserver;
import com.techstud.scheduleuniversity.kafka.KafkaProducer;
import com.techstud.scheduleuniversity.repository.jpa.StudentRepository;
import com.techstud.scheduleuniversity.repository.jpa.UniversityGroupRepository;
import com.techstud.scheduleuniversity.repository.mongo.ScheduleDayRepository;
import com.techstud.scheduleuniversity.repository.mongo.ScheduleRepository;
import com.techstud.scheduleuniversity.repository.mongo.ScheduleRepositoryFacade;
import com.techstud.scheduleuniversity.service.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

@ActiveProfiles("dev")
@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private UniversityGroupRepository universityGroupRepository;

    @Mock
    private ScheduleRepositoryFacade scheduleRepositoryFacade;

    @Mock
    private KafkaProducer kafkaProducer;

    @Mock
    private KafkaMessageObserver kafkaMessageObserver;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ScheduleDayRepository scheduleDayRepository;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    @BeforeEach
    void setUp() {
        Student studentWithSchedule = new Student("studentWithSchedule");
        studentWithSchedule.setId(1L);
        studentWithSchedule.setLastAction(LocalDate.now());
        studentWithSchedule.setScheduleMongoId("1");

        Student studentWithoutSchedule = new Student("studentWithoutSchedule");
        studentWithoutSchedule.setId(2L);
        studentWithoutSchedule.setLastAction(LocalDate.now());

        Mockito.when(studentRepository
                .findByUsername("studentWithSchedule"))
                .thenReturn(Optional.of(studentWithSchedule));

        Mockito.when(studentRepository
                .findByUsername("studentWithoutSchedule"))
                .thenReturn(Optional.of(studentWithoutSchedule));

        University universitySSAU = new University();
        universitySSAU.setId(1L);
        universitySSAU.setUrl("https://ssau.ru");
        universitySSAU.setVersion(1L);
        universitySSAU.setShortName("SSAU");
        universitySSAU.setFullName("Самарский национальный исследовательский университет им. С.П. Королёва");

        UniversityGroup universityGroup1104 = new UniversityGroup();
        universityGroup1104.setId(1L);
        universityGroup1104.setGroupCode("1104-150303D");
        universityGroup1104.setScheduleMongoId("1UG");
        universityGroup1104.setUniversity(universitySSAU);

        University universitySSEU = new University();
        universitySSEU.setId(2L);
        universitySSEU.setUrl("https://sseu.ru");
        universitySSEU.setVersion(1L);
        universitySSEU.setShortName("SSEU");
        universitySSEU.setFullName("Самарский государственный экономический университет");

        UniversityGroup universityGroupSSEU1 = new UniversityGroup();
        universityGroupSSEU1.setId(2L);
        universityGroupSSEU1.setGroupCode("МФН24оз1");
        universityGroupSSEU1.setScheduleMongoId("2UG");
        universityGroupSSEU1.setUniversity(universitySSEU);

        Mockito.when(universityGroupRepository
                .findByUniversityShortNameAndGroupCode("SSAU", "1104-150303D"))
                .thenReturn(Optional.of(universityGroup1104));

        Mockito.when(universityGroupRepository
                .findByUniversityShortNameAndGroupCode("SSEU", "МФН24оз1"))
                .thenReturn(Optional.of(universityGroupSSEU1));

        ScheduleDocument scheduleDocument = generateScheduleDocument();
    }

    @Test
    void importSchedule() throws ScheduleNotFoundException, StudentNotFoundException {

    }

    @Test
    void createSchedule() {
    }

    @Test
    void forceImportSchedule() {
    }

    @Test
    void deleteSchedule() {
    }

    @Test
    void deleteScheduleDay() {
    }

    @Test
    void deleteLesson() {
    }

    @Test
    void getScheduleById() {
    }

    @Test
    void getScheduleByStudentName() {
    }

    @Test
    void updateLesson() {
    }

    @Test
    void updateScheduleDay() {
    }

    ScheduleDocument generateScheduleDocument() {
        ScheduleDocument scheduleDocument = new ScheduleDocument();
        
        return scheduleDocument;
    }
}