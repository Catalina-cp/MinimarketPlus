package com.minimarket;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la entidad Usuario.
 *
 * Valida que:
 *  - El usuario se crea correctamente con sus datos y roles.
 *  - La autenticación simulada distingue credenciales válidas e inválidas.
 *  - Los roles determinan los permisos del usuario.
 *  - Intentos de acceso con usuario incorrecto o sin rol son rechazados.
 */
@DisplayName("Pruebas Unitarias - Usuario")
public class UsuarioTest {

    private Usuario admin;
    private Usuario cajero;
    private Usuario cliente;

    @BeforeEach
    void setUp() {
        admin = new Usuario();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setRoles(Set.of(new Rol("ADMIN")));

        cajero = new Usuario();
        cajero.setId(2L);
        cajero.setUsername("cajero1");
        cajero.setPassword("cajero123");
        cajero.setRoles(Set.of(new Rol("CAJERO")));

        cliente = new Usuario();
        cliente.setId(3L);
        cliente.setUsername("cliente1");
        cliente.setPassword("cliente123");
        cliente.setRoles(Set.of(new Rol("CLIENTE")));
    }

    //  Método auxiliar: simula autenticación básica 
    private boolean autenticar(Usuario usuario, String usernameIngresado, String passwordIngresada) {
        return usuario.getUsername().equals(usernameIngresado)
            && usuario.getPassword().equals(passwordIngresada);
    }

    //  Test 1: Creación correcta de usuario 

    @Test
    @DisplayName("Debe crear un usuario ADMIN con todos sus datos")
    public void testCrearUsuario() {
        Set<Rol> roles = Set.of(new Rol("ADMIN"));
        Usuario usuario = new Usuario();
        usuario.setUsername("adminUser");
        usuario.setPassword("securePassword123");
        usuario.setRoles(roles);

        assertNotNull(usuario);
        assertEquals("adminUser", usuario.getUsername());
        assertEquals("securePassword123", usuario.getPassword());
        assertEquals(1, usuario.getRoles().size());
        assertTrue(usuario.getRoles().stream()
                .anyMatch(role -> role.getNombre().equals("ADMIN")));
    }

    //  Test 2: Autenticación exitosa con credenciales correctas 

    @Test
    @DisplayName("Autenticación exitosa con credenciales válidas")
    void testAutenticacionExitosa() {
        boolean resultado = autenticar(admin, "admin", "admin123");
        assertTrue(resultado,
                "La autenticación debe ser exitosa con credenciales correctas");
    }

    //  Test 3: Autenticación fallida con contraseña incorrecta 

    @Test
    @DisplayName("Autenticación fallida con contraseña incorrecta")
    void testAutenticacionFallidaPasswordIncorrecta() {
        boolean resultado = autenticar(admin, "admin", "passwordErronea");
        assertFalse(resultado,
                "La autenticación debe fallar con contraseña incorrecta");
    }

    //  Test 4: Autenticación fallida con usuario inexistente 

    @Test
    @DisplayName("Autenticación fallida con nombre de usuario incorrecto")
    void testAutenticacionFallidaUsuarioIncorrecto() {
        boolean resultado = autenticar(admin, "usuarioFalso", "admin123");
        assertFalse(resultado,
                "La autenticación debe fallar si el username no coincide");
    }

    //  Test 5: Roles correctamente asignados por tipo de usuario 

    @Test
    @DisplayName("Cada tipo de usuario debe tener su rol correspondiente")
    void testRolesAsignadosCorrectamente() {
        assertTrue(admin.getRoles().stream()
                .anyMatch(r -> r.getNombre().equals("ADMIN")), "Admin debe tener rol ADMIN");

        assertTrue(cajero.getRoles().stream()
                .anyMatch(r -> r.getNombre().equals("CAJERO")), "Cajero debe tener rol CAJERO");

        assertTrue(cliente.getRoles().stream()
                .anyMatch(r -> r.getNombre().equals("CLIENTE")), "Cliente debe tener rol CLIENTE");
    }

    //  Test 6: Comparación de dos usuarios con mismos datos 

    @Test
    @DisplayName("Dos usuarios con el mismo ID y username deben ser equivalentes")
    public void testEquals() {
        Usuario usuario1 = new Usuario();
        usuario1.setId(1L);
        usuario1.setUsername("adminUser");
        usuario1.setPassword("securePassword123");

        Usuario usuario2 = new Usuario();
        usuario2.setId(1L);
        usuario2.setUsername("adminUser");
        usuario2.setPassword("securePassword123");

        assertEquals(usuario1.getId(), usuario2.getId());
        assertEquals(usuario1.getUsername(), usuario2.getUsername());
        assertEquals(usuario1.getPassword(), usuario2.getPassword());
    }

    //  Test 7: Asignación de múltiples roles 

    @Test
    @DisplayName("Un usuario puede tener múltiples roles")
    public void testAgregarRoles() {
        Usuario usuario = new Usuario();
        usuario.setUsername("superUser");
        usuario.setPassword("pass123");
        usuario.setRoles(Set.of(new Rol("USER"), new Rol("ADMIN")));

        assertEquals(2, usuario.getRoles().size());
        assertTrue(usuario.getRoles().stream()
                .anyMatch(role -> role.getNombre().equals("USER")));
        assertTrue(usuario.getRoles().stream()
                .anyMatch(role -> role.getNombre().equals("ADMIN")));
    }

    //  Test 8: Usuario sin roles no tiene acceso privilegiado 

    @Test
    @DisplayName("Usuario sin roles no debe tener acceso a funciones privilegiadas")
    void testUsuarioSinRolesNoTieneAcceso() {
        Usuario sinRoles = new Usuario();
        sinRoles.setUsername("anonimo");
        sinRoles.setPassword("pass");
        sinRoles.setRoles(Set.of());

        boolean esAdmin  = sinRoles.getRoles().stream()
                .anyMatch(r -> r.getNombre().equals("ADMIN"));
        boolean esCajero = sinRoles.getRoles().stream()
                .anyMatch(r -> r.getNombre().equals("CAJERO"));

        assertFalse(esAdmin,  "Sin roles no debe tener privilegio ADMIN");
        assertFalse(esCajero, "Sin roles no debe tener privilegio CAJERO");
    }
}
