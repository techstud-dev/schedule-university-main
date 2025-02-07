package com.techstud.scheduleuniversity.security;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Primary
public class AuditorAwareImpl implements AuditorAware<String> {

    public Optional<String> getCurrentAuditor() {
        return SecurityContextHolder.getContext().getAuthentication() == null ?
                Optional.of("system") :
                Optional.of(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
