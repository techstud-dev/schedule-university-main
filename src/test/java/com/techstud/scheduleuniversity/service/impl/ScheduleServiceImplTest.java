package com.techstud.scheduleuniversity.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstud.scheduleuniversity.dao.HashableDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDayDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleDocument;
import com.techstud.scheduleuniversity.dao.document.schedule.ScheduleObjectDocument;
import com.techstud.scheduleuniversity.dao.entity.Student;
import com.techstud.scheduleuniversity.dao.entity.University;
import com.techstud.scheduleuniversity.dao.entity.UniversityGroup;
import com.techstud.scheduleuniversity.dto.ScheduleType;
import com.techstud.scheduleuniversity.dto.parser.response.ScheduleParserResponse;
import com.techstud.scheduleuniversity.exception.ScheduleNotFoundException;
import com.techstud.scheduleuniversity.exception.StudentNotFoundException;
import com.techstud.scheduleuniversity.kafka.KafkaMessageObserver;
import com.techstud.scheduleuniversity.kafka.KafkaProducer;
import com.techstud.scheduleuniversity.repository.jpa.StudentRepository;
import com.techstud.scheduleuniversity.repository.jpa.UniversityGroupRepository;
import com.techstud.scheduleuniversity.repository.mongo.ScheduleDayRepository;
import com.techstud.scheduleuniversity.repository.mongo.ScheduleRepository;
import com.techstud.scheduleuniversity.repository.mongo.ScheduleRepositoryFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

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

    private ObjectMapper objectMapper = new ObjectMapper();

    private String ssauScheduleMongoId;
    private String sseuScheduleMongoId;


    @BeforeEach
    void setUp() throws Exception {

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

        ScheduleDocument ssauScheduleDocument = generateScheduleDocument();
        ScheduleDocument sseuScheduleDocument = generateScheduleDocument();
        this.ssauScheduleMongoId = ssauScheduleDocument.getId();
        this.sseuScheduleMongoId = sseuScheduleDocument.getId();

        Mockito.when(scheduleRepository.findById(ssauScheduleMongoId))
                .thenReturn(Optional.of(ssauScheduleDocument));

        Mockito.when(scheduleRepository.findById(sseuScheduleMongoId))
                .thenReturn(Optional.of(sseuScheduleDocument));

        Student ssauStudentWithSchedule = new Student("ssauStudentWithSchedule");
        ssauStudentWithSchedule.setId(1L);
        ssauStudentWithSchedule.setLastAction(LocalDate.now());
        ssauStudentWithSchedule.setScheduleMongoId(ssauScheduleMongoId);

        Student studentWithoutSchedule = new Student("studentWithoutSchedule");
        studentWithoutSchedule.setId(2L);
        studentWithoutSchedule.setLastAction(LocalDate.now());

        Student sseuStudentWithSchedule = new Student("ssauStudentWithSchedule");
        sseuStudentWithSchedule.setId(3L);
        sseuStudentWithSchedule.setLastAction(LocalDate.now());
        sseuStudentWithSchedule.setScheduleMongoId(sseuScheduleMongoId);

        Mockito.when(studentRepository
                        .findByUsername("ssauStudentWithSchedule"))
                .thenReturn(Optional.of(ssauStudentWithSchedule));

        Mockito.when(studentRepository
                        .findByUsername("studentWithoutSchedule"))
                .thenReturn(Optional.of(studentWithoutSchedule));

        Mockito.when(studentRepository
                        .findByUsername("sseuStudentWithSchedule"))
                .thenReturn(Optional.of(sseuStudentWithSchedule));
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

    ScheduleDocument generateScheduleDocument() throws Exception {
        ScheduleDocument scheduleDocument = new ScheduleDocument();

        Map<DayOfWeek, ScheduleDayDocument> evenWeekSchedule = Map.of(
                DayOfWeek.MONDAY, generateScheduleDayDocument(),
                DayOfWeek.TUESDAY, generateScheduleDayDocument(),
                DayOfWeek.WEDNESDAY, generateScheduleDayDocument(),
                DayOfWeek.THURSDAY, generateScheduleDayDocument(),
                DayOfWeek.FRIDAY, generateScheduleDayDocument(),
                DayOfWeek.SATURDAY, generateScheduleDayDocument(),
                DayOfWeek.SUNDAY, generateScheduleDayDocument()
        );

        Map<DayOfWeek, ScheduleDayDocument> oddWeekSchedule = Map.of(
                DayOfWeek.MONDAY, generateScheduleDayDocument(),
                DayOfWeek.TUESDAY, generateScheduleDayDocument(),
                DayOfWeek.WEDNESDAY, generateScheduleDayDocument(),
                DayOfWeek.THURSDAY, generateScheduleDayDocument(),
                DayOfWeek.FRIDAY, generateScheduleDayDocument(),
                DayOfWeek.SATURDAY, generateScheduleDayDocument(),
                DayOfWeek.SUNDAY, generateScheduleDayDocument()
        );

        scheduleDocument.setEvenWeekSchedule(evenWeekSchedule);
        scheduleDocument.setOddWeekSchedule(oddWeekSchedule);
        scheduleDocument.setId(UUID.randomUUID().toString());
        computeAndSetHash(scheduleDocument);
        return scheduleDocument;
    }

    ScheduleDayDocument generateScheduleDayDocument() throws Exception {
        ScheduleDayDocument scheduleDayDocument = new ScheduleDayDocument();
        scheduleDayDocument.setId(UUID.randomUUID().toString());
        scheduleDayDocument.setDate(new Date());
        scheduleDayDocument.setLessons(generateLessons());
        computeAndSetHash(scheduleDayDocument);
        return scheduleDayDocument;
    }

    Map<String, List<ScheduleObjectDocument>> generateLessons() throws Exception {
        Map<String, List<ScheduleObjectDocument>> lessons = new LinkedHashMap<>();
        lessons.put(UUID.randomUUID().toString(), getRandomLessonPool());
        lessons.put(UUID.randomUUID().toString(), getRandomLessonPool());
        lessons.put(UUID.randomUUID().toString(), getRandomLessonPool());
        lessons.put(UUID.randomUUID().toString(), getRandomLessonPool());
        lessons.put(UUID.randomUUID().toString(), getRandomLessonPool());
        return lessons;
    }


    private void computeAndSetHash(HashableDocument entity) throws Exception {
        String json = objectMapper.writeValueAsString(entity);
        String hash = computeSHA256Hash(json);
        entity.setHash(hash);
    }

    private String computeSHA256Hash(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    List<ScheduleObjectDocument> getRandomLessonPool() throws Exception {
        List<ScheduleObjectDocument> lessonList = new ArrayList<>();

        ScheduleObjectDocument lesson1 = new ScheduleObjectDocument();
        lesson1.setId(UUID.randomUUID().toString());
        lesson1.setName("Высшая математика");
        lesson1.setPlace("417-3");
        lesson1.setTeacher("Триндюк Т.А.");
        lesson1.setType(ScheduleType.LECTURE);
        computeAndSetHash(lesson1);
        lessonList.add(lesson1);

        ScheduleObjectDocument lesson2 = new ScheduleObjectDocument();
        lesson2.setId(UUID.randomUUID().toString());
        lesson2.setName("Политология");
        lesson2.setPlace("204-3");
        lesson2.setTeacher("Стычков И.К.");
        lesson2.setType(ScheduleType.LECTURE);
        computeAndSetHash(lesson2);
        lessonList.add(lesson2);

        ScheduleObjectDocument lesson3 = new ScheduleObjectDocument();
        lesson3.setId(UUID.randomUUID().toString());
        lesson3.setName("Начертательная геометрия");
        lesson3.setPlace("425-3");
        lesson3.setTeacher("Жемкова Ю.А.");
        lesson3.setType(ScheduleType.LECTURE);
        computeAndSetHash(lesson3);
        lessonList.add(lesson3);

        ScheduleObjectDocument lesson4 = new ScheduleObjectDocument();
        lesson4.setId(UUID.randomUUID().toString());
        lesson4.setName("Линейная алгебра");
        lesson4.setPlace("306-3");
        lesson4.setTeacher("Васильева О.А.");
        lesson4.setType(ScheduleType.PRACTICE);
        computeAndSetHash(lesson4);
        lessonList.add(lesson4);

        ScheduleObjectDocument lesson5 = new ScheduleObjectDocument();
        lesson5.setId(UUID.randomUUID().toString());
        lesson5.setName("Информатика");
        lesson5.setPlace("102-3");
        lesson5.setTeacher("Кисегач Л.А.");
        lesson5.setType(ScheduleType.LAB);
        computeAndSetHash(lesson5);
        lessonList.add(lesson5);

        ScheduleObjectDocument lesson6 = new ScheduleObjectDocument();
        lesson6.setId(UUID.randomUUID().toString());
        lesson6.setName("Физика");
        lesson6.setPlace("301-3");
        lesson6.setTeacher("Кузнецова Л.А.");
        lesson6.setType(ScheduleType.LECTURE);
        computeAndSetHash(lesson6);
        lessonList.add(lesson6);

        ScheduleObjectDocument lesson7 = new ScheduleObjectDocument();
        lesson7.setId(UUID.randomUUID().toString());
        lesson7.setName("История");
        lesson7.setPlace("201-3");
        lesson7.setTeacher("Банниккова Н.Ф.");
        lesson7.setType(ScheduleType.PRACTICE);
        computeAndSetHash(lesson7);
        lessonList.add(lesson7);

        ScheduleObjectDocument lesson8 = new ScheduleObjectDocument();
        lesson8.setId(UUID.randomUUID().toString());
        lesson8.setName("Физкультура");
        lesson8.setPlace("Спортзал");
        lesson8.setType(ScheduleType.UNKNOWN);
        computeAndSetHash(lesson8);
        lessonList.add(lesson8);

        ScheduleObjectDocument lesson9 = new ScheduleObjectDocument();
        lesson9.setId(UUID.randomUUID().toString());
        lesson9.setName("Сопротивление материалов");
        lesson9.setPlace("211-3");
        lesson9.setTeacher("Кирпичев В.А.");
        lesson9.setType(ScheduleType.LAB);
        computeAndSetHash(lesson9);
        lessonList.add(lesson9);

        ScheduleObjectDocument lesson10 = new ScheduleObjectDocument();
        lesson10.setId(UUID.randomUUID().toString());
        lesson10.setName("Сопротивление материалов");
        lesson10.setPlace("211-3");
        lesson10.setTeacher("Кирпичев В.А.");
        lesson10.setType(ScheduleType.PRACTICE);
        computeAndSetHash(lesson10);
        lessonList.add(lesson10);

        ScheduleObjectDocument lesson11 = new ScheduleObjectDocument();
        lesson11.setId(UUID.randomUUID().toString());
        lesson11.setName("Сопротивление материалов");
        lesson11.setPlace("211-3");
        lesson11.setTeacher("Кирпичев В.А.");
        lesson11.setType(ScheduleType.LECTURE);
        computeAndSetHash(lesson11);
        lessonList.add(lesson11);

        List<ScheduleObjectDocument> result = new ArrayList<>();

        Random random = new Random();
        int index = random.nextInt(lessonList.size());
        result.add(lessonList.get(index));

        return result;
    }
}