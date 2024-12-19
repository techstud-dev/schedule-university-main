package com.techstud.scheduleuniversity.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "API для работы с пользователями")
public class UserController {

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public EntityModel<Object> getUserProfile(Principal principal) {
        log.info("Incoming request. User: {}", principal.getName());
        return EntityModel.of(new Object());
    }
}
