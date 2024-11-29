package com.techstud.scheduleuniversity.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techstud.scheduleuniversity.dto.fetcher.GroupData;
import com.techstud.scheduleuniversity.dto.fetcher.api.response.SsauApiGroupDataResponse;
import com.techstud.scheduleuniversity.service.GroupFetcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;

import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
@Slf4j
@RequiredArgsConstructor
public class SsauGroupDataFetchService implements GroupFetcherService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<GroupData> fetchGroupsData() {
        List<GroupData> groupDataList = new ArrayList<>();

        // Укажите путь к драйверу ChromeDriver
        System.setProperty("webdriver.chrome.driver", "src/main/resources/drivers/yandexdriver");



        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\Users\\Funtikov\\AppData\\Local\\Yandex\\YandexBrowser\\Application\\browser.exe");
        WebDriver driver = new ChromeDriver(options);
        try {
            // Открываем страницу
            driver.get("https://ssau.ru");

            // Получаем куки из браузера
            Set<Cookie> seleniumCookies = driver.manage().getCookies();
            StringBuilder cookieHeader = new StringBuilder();
            String csrfToken = null;

            for (Cookie cookie : seleniumCookies) {
                cookieHeader.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");
                if ("XSRF-TOKEN".equals(cookie.getName())) {
                    csrfToken = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                }
            }

            if (csrfToken == null) {
                log.error("CSRF token not found in cookies");
                return groupDataList;
            }

            log.info("Extracted CSRF Token: {}", csrfToken);

            String fullCookies = cookieHeader.toString();

            // Настраиваем HttpClient
            CloseableHttpClient httpClient = HttpClients.createDefault();

            // Выполняем POST-запросы
            for (int i = 1; i < 10; i++) {
                for (int j = 1; j < 10; j++) {
                    String currentPattern = String.format("%d%d", i, j);
                    HttpPost httpPost = new HttpPost("https://ssau.ru/rasp/search");
                    httpPost.setHeader("Accept", "application/json");
                    httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                    httpPost.setHeader("X-Csrf-Token", csrfToken);
                    httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
                    httpPost.setHeader("Referer", "https://ssau.ru/rasp");
                    httpPost.setHeader("Origin", "https://ssau.ru");
                    httpPost.setHeader("Cookie", fullCookies);

                    List<BasicNameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair("text", currentPattern));
                    httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

                    try (CloseableHttpResponse postResponse = httpClient.execute(httpPost)) {
                        int postStatusCode = postResponse.getCode();
                        log.info("POST response code: {}", postStatusCode);

                        if (postStatusCode == 200) {
                            String responseBody = EntityUtils.toString(postResponse.getEntity(), StandardCharsets.UTF_8);
                            log.info("Response body: {}", responseBody);

                            // Обработка JSON-ответа
                            List<SsauApiGroupDataResponse> apiGroupDataResponses = objectMapper.readValue(
                                    responseBody,
                                    new TypeReference<List<SsauApiGroupDataResponse>>() {}
                            );

                            for (SsauApiGroupDataResponse apiResponse : apiGroupDataResponses) {
                                GroupData groupData = new GroupData(apiResponse.text(), apiResponse.id().toString());
                                groupDataList.add(groupData);
                            }
                        } else {
                            log.error("Failed to fetch group data from SSAU API. Status code: {}", postStatusCode);
                        }
                    } catch (Exception e) {
                        log.error("Failed to fetch group data from SSAU API", e);
                        break;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error occurred while fetching groups data", e);
        } finally {
            driver.quit();
        }

        return groupDataList;
    }

}
