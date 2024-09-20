package com.gamq.deportes.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class AuditorAwareimpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        String userLogged = "Admin";
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        if (attr != null) {
            if (attr.getRequest() != null) {
                if (attr.getRequest().getSession() != null) {
                    userLogged = attr.getRequest().getSession().getAttribute("usuario").toString();
                }
            }
        }
        return Optional.of(userLogged);
        //return Optional.empty();
    }
}
