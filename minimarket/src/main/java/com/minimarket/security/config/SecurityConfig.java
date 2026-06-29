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

    /**
     * Define las reglas de autenticación y autorización para cada endpoint.
     * Roles:
     *   - ADMIN     → gestión de productos, inventario, categorías, usuarios
     *   - CAJERO    → generar ventas y ver carritos
     *   - CLIENTE   → ver productos y gestionar su propio carrito
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Deshabilitado para API REST
            .authorizeHttpRequests(auth -> auth

                // ── Consola H2 (solo para desarrollo) ──────────────────────
                .requestMatchers("/h2-console/**").permitAll()

                // ── Productos ───────────────────────────────────────────────
                // Cualquier usuario autenticado puede VER productos
                .requestMatchers(HttpMethod.GET, "/api/productos/**").authenticated()
                // Solo ADMIN puede CREAR, MODIFICAR o ELIMINAR productos
                .requestMatchers(HttpMethod.POST,   "/api/productos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/productos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMIN")

                // ── Inventario ──────────────────────────────────────────────
                // Solo ADMIN puede gestionar movimientos de inventario
                .requestMatchers("/api/inventarios/**").hasRole("ADMIN")

                // ── Ventas ──────────────────────────────────────────────────
                // Solo CAJERO puede crear y ver ventas
                .requestMatchers("/api/ventas/**").hasAnyRole("CAJERO", "ADMIN")

                // ── Carrito ─────────────────────────────────────────────────
                // CLIENTE y CAJERO pueden gestionar carritos
                .requestMatchers("/api/carritos/**").hasAnyRole("CLIENTE", "CAJERO", "ADMIN")

                // ── Categorías ──────────────────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/categorias/**").authenticated()
                .requestMatchers("/api/categorias/**").hasRole("ADMIN")

                // ── Usuarios ─────────────────────────────────────────────────
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {}) // Autenticación HTTP Basic para pruebas
            .headers(headers -> headers.frameOptions(frame -> frame.disable())); // Para H2 console

        return http.build();
    }

    /**
     * Codificador de contraseñas con BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Usuarios en memoria para pruebas y desarrollo.
     * En producción esto se reemplaza por UserDetailsService con base de datos.
     */
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
