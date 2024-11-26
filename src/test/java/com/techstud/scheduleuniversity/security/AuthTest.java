package com.techstud.scheduleuniversity.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.dto.response.schedule.Schedule;
import com.techstud.scheduleuniversity.mapper.ScheduleMapper;
import com.techstud.scheduleuniversity.service.ScheduleService;
import com.techstud.scheduleuniversity.validation.RequestValidationService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Description;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@ActiveProfiles("dev")
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@Description("Тесты JWT аутентификации")
public class AuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.audience}")
    private String audience;

    @MockBean
    private ScheduleService scheduleService;

    @MockBean
    private RequestValidationService requestValidationService;

    @MockBean
    private ScheduleMapper scheduleMapper;

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17.0")
            .withDatabaseName("schedule_university")
            .withUsername("abuser")
            .withPassword("abuser");

    @Container
    private static final MongoDBContainer mongoContainer = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void registerPostgresProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl);
    }

    private String generateToken(String username, List<String> roles) {

        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().findAndRegisterModules().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Description("Тест успешной аутентификации")
    public void testSuccessAuth() throws Exception {

        ImportDto importDto = new ImportDto();

        ApiRequest<ImportDto> importRequest = new ApiRequest<>();
        importRequest.setData(importDto);

        com.techstud.scheduleuniversity.dao.document.schedule.Schedule documentSchedule =
                new com.techstud.scheduleuniversity.dao.document.schedule.Schedule();
        Schedule schedule = new Schedule();

        when(scheduleService.importSchedule(any(ImportDto.class), eq("test-user"))).thenReturn(documentSchedule);
        when(scheduleMapper.toResponse(any(com.techstud.scheduleuniversity.dao.document.schedule.Schedule.class)))
                .thenReturn(schedule);

        String token = generateToken("test-user", List.of("USER"));

        mockMvc.perform(post("/api/v1/schedule/import")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(asJsonString(importRequest)))
                .andExpect(status().isOk());

        verify(scheduleService).importSchedule(any(ImportDto.class), eq("test-user"));
        verify(scheduleMapper).toResponse(any(com.techstud.scheduleuniversity.dao.document.schedule.Schedule.class));
    }

    @Test
    @Description("Тест неуспешной аутентификации с неверным токеном")
    public void testInvalidToken() throws Exception {

        ImportDto importDto = new ImportDto();

        ApiRequest<ImportDto> importRequest = new ApiRequest<>();
        importRequest.setData(importDto);

        String invalidSecret = "33948979fe08bd05a5b297ad2a4d024b627c4effdcf40fc1d1262cc4054c09c5e93956a8421341c47cdab8b0d9dd74134ceaebfc9041a9d926079fe33b2ecb7e997dc9d94f0b8f83ca6ac4d9101478a6fe51e6dc2ccc4bf589033fd0a704d2c2e46976a01bb874c1772bae6be7bbb18990c70e65f2df51dfa1ddee5841ad927aed325939a3b0d0e9d50a0a47ec585a6d8ecf46864a7bba804d35a8bfb235cce207a60543a4772883e44d86ca425f489e04caf788e74516c9a1ccf64114ef54f5048be5682bdc63f7b465e44bdc23874570da80d465ace5cfde5958df19a438aeafe3a0f051cc8b59f169909d0da733483d80962a213db9789f84e8608d7655c8";
        SecretKey secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(invalidSecret));

        String invalidToken = Jwts.builder()
                .setSubject("test-user")
                .claim("roles", List.of("USER"))
                .setIssuer(issuer)
                .setAudience(audience)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        mockMvc.perform(post("/api/v1/schedule/import")
                        .header("Authorization", "Bearer " + invalidToken)
                        .contentType("application/json")
                        .content(asJsonString(importRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Description("Тест неуспешной аутентификации при отсутствии токена")
    public void testMissingToken() throws Exception {

        ImportDto importDto = new ImportDto();

        ApiRequest<ImportDto> importRequest = new ApiRequest<>();
        importRequest.setData(importDto);

        mockMvc.perform(post("/api/v1/schedule/import")
                        .contentType("application/json")
                        .content(asJsonString(importRequest)))
                .andExpect(status().isForbidden());
    }
}
