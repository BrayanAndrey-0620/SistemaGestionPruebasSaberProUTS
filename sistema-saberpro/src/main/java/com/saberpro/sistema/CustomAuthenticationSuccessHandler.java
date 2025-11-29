package com.saberpro.sistema;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // Asegúrate de importar esto
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        
        // 1. Obtener datos de la autenticación
        String username = authentication.getName();
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        
        HttpSession session = request.getSession();
        
        // 2. Guardar el username en la sesión para que los controladores lo usen (CRÍTICO)
        session.setAttribute("username", username); 
        
        if (roles.contains("COORDINACION")) {
            session.setAttribute("rol", "COORDINACION"); // Guardar Rol
            response.sendRedirect("/coordinacion/dashboard");
        } else if (roles.contains("ESTUDIANTE")) {
            session.setAttribute("rol", "ESTUDIANTE"); // Guardar Rol
            response.sendRedirect("/estudiante/dashboard");
        } else {
            // Rol no reconocido
            response.sendRedirect("/");
        }
    }
}