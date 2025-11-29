package com.saberpro.sistema.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.saberpro.sistema.entity.Usuario;
import com.saberpro.sistema.service.UsuarioService;

import java.util.Optional;

// 💡 IMPORTS NECESARIOS DE SPRING SECURITY (¡Los que faltaban!)
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // <--- ESTE ES EL QUE FALTABA
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String loginPage() {
        return "login";
    }
   
    @PostMapping("/login")
    public String login(@RequestParam String username, 
                        @RequestParam String password, 
                        HttpSession session, 
                        Model model) {
        
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
        
        if (usuarioOpt.isPresent() && usuarioOpt.get().getPassword().equals(password)) {
            Usuario usuario = usuarioOpt.get();
            String rol = usuario.getRol();
            
            // 🚨 PASO CRÍTICO: AUTENTICAR EN EL CONTEXTO DE SPRING SECURITY
            // 1. Crear la lista de permisos (Role)
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario.getUsername(), 
                null, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol)) // Debe empezar con ROLE_
            );
            
            // 2. Establecer la autenticación en el contexto de seguridad (¡Esto evita el bucle!)
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // (Tu lógica de sesión manual - opcional, pero mantiene tus atributos)
            session.setAttribute("usuario", usuario);
            session.setAttribute("username", usuario.getUsername());
            session.setAttribute("rol", rol);
            session.setAttribute("nombre", usuario.getNombre());
            
            if ("COORDINACION".equals(rol)) {
                return "redirect:/coordinacion/coordinacion-dashboard"; // Ajusta esta ruta si es necesario
            } else if ("ESTUDIANTE".equals(rol)) {
                return "redirect:/estudiante/dashboard-estudiante"; // Ajusta esta ruta si es necesario
            }
        }
        
        model.addAttribute("error", "Credenciales inválidas");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "acceso-denegado";
    }
}