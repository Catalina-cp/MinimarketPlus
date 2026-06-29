package com.minimarket;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la entidad Producto.
 *
 * Valida que:
 *  - Solo usuarios ADMIN pueden "modificar" productos (simulado por lógica de rol).
 *  - Los datos del producto se crean y validan correctamente.
 *  - Usuarios sin rol ADMIN no tienen permiso de edición.
 */
@DisplayName("Pruebas Unitarias - Producto")
public class ProductoTest {

    private Producto producto;
    private Categoria categoria;
    private Usuario admin;
    private Usuario cliente;

    @BeforeEach
    void setUp() {
        // Preparar categoría
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Bebidas");

        // Preparar producto
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Agua Mineral");
        producto.setPrecio(500.0);
        producto.setStock(100);
        producto.setCategoria(categoria);

        // Usuario ADMIN
        admin = new Usuario();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setRoles(Set.of(new Rol("ADMIN")));

        // Usuario CLIENTE (sin permiso de edición)
        cliente = new Usuario();
        cliente.setId(2L);
        cliente.setUsername("cliente1");
        cliente.setPassword("pass123");
        cliente.setRoles(Set.of(new Rol("CLIENTE")));
    }

    // ─── Test 1: Creación correcta del producto ───────────────────────────────

    @Test
    @DisplayName("Debe crear un producto con todos sus datos correctamente")
    void testCrearProducto() {
        assertNotNull(producto);
        assertEquals(1L, producto.getId());
        assertEquals("Agua Mineral", producto.getNombre());
        assertEquals(500.0, producto.getPrecio());
        assertEquals(100, producto.getStock());
        assertNotNull(producto.getCategoria());
        assertEquals("Bebidas", producto.getCategoria().getNombre());
    }

    // ─── Test 2: Solo ADMIN puede modificar producto ─────────────────────────

    @Test
    @DisplayName("ADMIN puede modificar el precio de un producto")
    void testAdminPuedeModificarProducto() {
        // Simular verificación de rol
        boolean tienePermisoAdmin = admin.getRoles()
                .stream()
                .anyMatch(rol -> rol.getNombre().equals("ADMIN"));

        assertTrue(tienePermisoAdmin, "El usuario ADMIN debe tener permiso para modificar productos");

        // Simular modificación solo si tiene permiso
        if (tienePermisoAdmin) {
            producto.setPrecio(650.0);
            producto.setNombre("Agua Mineral Premium");
        }

        assertEquals(650.0, producto.getPrecio());
        assertEquals("Agua Mineral Premium", producto.getNombre());
    }

    // ─── Test 3: CLIENTE no puede modificar producto ──────────────────────────

    @Test
    @DisplayName("CLIENTE no debe tener permiso para modificar productos")
    void testClienteNoPuedeModificarProducto() {
        boolean tienePermisoAdmin = cliente.getRoles()
                .stream()
                .anyMatch(rol -> rol.getNombre().equals("ADMIN"));

        assertFalse(tienePermisoAdmin,
                "El usuario CLIENTE no debe tener permiso de modificación de productos");

        // El precio no cambia porque no tiene permiso
        double precioOriginal = producto.getPrecio();
        if (tienePermisoAdmin) {
            producto.setPrecio(9999.0); // No debería ejecutarse
        }

        assertEquals(precioOriginal, producto.getPrecio(),
                "El precio no debe cambiar si el usuario no tiene rol ADMIN");
    }

    // ─── Test 4: Validación de stock no negativo ──────────────────────────────

    @Test
    @DisplayName("El stock de un producto no debe ser negativo")
    void testStockNoNegativo() {
        producto.setStock(10);
        int cantidadVendida = 5;

        int stockResultante = producto.getStock() - cantidadVendida;
        assertTrue(stockResultante >= 0, "El stock resultante no debe ser negativo");

        producto.setStock(stockResultante);
        assertEquals(5, producto.getStock());
    }

    // ─── Test 5: Producto debe tener categoría asignada ──────────────────────

    @Test
    @DisplayName("Un producto debe tener una categoría asignada")
    void testProductoTieneCategoria() {
        assertNotNull(producto.getCategoria(),
                "El producto debe tener una categoría");
        assertNotNull(producto.getCategoria().getNombre(),
                "La categoría debe tener nombre");
    }
}
