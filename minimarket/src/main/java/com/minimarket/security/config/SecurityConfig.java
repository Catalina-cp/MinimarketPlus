package com.minimarket.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Deshabilitado para API REST
            .authorizeHttpRequests(auth -> auth

                .requestMatchers("/h2-console/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/productos/**").authenticated()
                .requestMatchers(HttpMethod.POST,   "/api/productos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/productos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMIN")
                .requestMatchers("/api/inventarios/**").hasRole("ADMIN")
                .requestMatchers("/api/ventas/**").hasAnyRole("CAJERO", "ADMIN")
                .requestMatchers("/api/carritos/**").hasAnyRole("CLIENTE", "CAJERO", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/categorias/**").authenticated()
                .requestMatchers("/api/categorias/**").hasRole("ADMIN")
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")


                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {}) 
            .headers(headers -> headers.frameOptions(frame -> frame.disable())); // Para H2 console

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        UserDetails cajero = User.builder()
                .username("cajero")
                .password(encoder.encode("cajero123"))
                .roles("CAJERO")
                .build();

        UserDetails cliente = User.builder()
                .username("cliente")
                .password(encoder.encode("cliente123"))
                .roles("CLIENTE")
                .build();

        return new InMemoryUserDetailsManager(admin, cajero, cliente);
    }
}
