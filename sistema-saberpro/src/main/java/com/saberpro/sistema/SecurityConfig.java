package com.saberpro.sistema;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	private final CustomAuthenticationSuccessHandler successHandler;
	public SecurityConfig(CustomAuthenticationSuccessHandler successHandler) {
	    this.successHandler = successHandler;
	} 
	
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/coordinacion/**").hasAuthority("COORDINACION")
                .requestMatchers("/estudiante/**").hasAuthority("ESTUDIANTE")
                
                .anyRequest().authenticated()
            		)
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(successHandler) // <--- ¡NUEVA LÍNEA CLAVE!
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> 
                    response.sendRedirect("/login")
                )
            );
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 💡 CRÍTICO: Indica a Spring Security que acepte contraseñas en texto plano.
        // ADVERTENCIA: Solo para desarrollo, en producción es inseguro.
        return NoOpPasswordEncoder.getInstance(); 
    }
}


