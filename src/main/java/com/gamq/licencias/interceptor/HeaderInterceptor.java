package com.gamq.licencias.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

@Component("headerInterceptor")
public class HeaderInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        List<String> headersNames = Collections.list(request.getHeaderNames());
        boolean existeHeaderNombreUsuario = headersNames.contains("usuario");
        if(existeHeaderNombreUsuario) {
            String usuarioAlmacenNombre = request.getHeader("usuario");
            request.getSession().setAttribute("usuario", usuarioAlmacenNombre);
        }else {
            request.getSession().setAttribute("usuario", "");
        }
        return true;
    }
}
