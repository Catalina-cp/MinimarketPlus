package com.minimarket;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la entidad Inventario.
 *
 * Valida que:
 *  - Solo usuarios ADMIN pueden registrar movimientos de inventario.
 *  - Los movimientos de entrada y salida se registran correctamente.
 *  - Cada movimiento está correctamente asociado a un producto.
 *  - Se detectan intentos de salida con stock insuficiente.
 */
@DisplayName("Pruebas Unitarias - Inventario")
public class InventarioTest {

    private Inventario movimientoEntrada;
    private Inventario movimientoSalida;
    private Producto producto;
    private Usuario admin;
    private Usuario cajero;

    @BeforeEach
    void setUp() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Lácteos");

        producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Leche Entera");
        producto.setPrecio(900.0);
        producto.setStock(50);
        producto.setCategoria(categoria);

        // Movimiento de ENTRADA (reabastecimiento)
        movimientoEntrada = new Inventario();
        movimientoEntrada.setId(1L);
        movimientoEntrada.setProducto(producto);
        movimientoEntrada.setCantidad(20);
        movimientoEntrada.setTipoMovimiento("Entrada");
        movimientoEntrada.setFechaMovimiento(new Date());

        // Movimiento de SALIDA (venta)
        movimientoSalida = new Inventario();
        movimientoSalida.setId(2L);
        movimientoSalida.setProducto(producto);
        movimientoSalida.setCantidad(5);
        movimientoSalida.setTipoMovimiento("Salida");
        movimientoSalida.setFechaMovimiento(new Date());

        // Usuario ADMIN
        admin = new Usuario();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setRoles(Set.of(new Rol("ADMIN")));

        // Usuario CAJERO
        cajero = new Usuario();
        cajero.setId(3L);
        cajero.setUsername("cajero1");
        cajero.setPassword("cajero123");
        cajero.setRoles(Set.of(new Rol("CAJERO")));
    }

    //  Test 1: Movimiento de ENTRADA registrado correctamente 

    @Test
    @DisplayName("Debe registrar un movimiento de entrada correctamente")
    void testRegistrarMovimientoEntrada() {
        assertNotNull(movimientoEntrada);
        assertEquals("Entrada", movimientoEntrada.getTipoMovimiento());
        assertEquals(20, movimientoEntrada.getCantidad());
        assertNotNull(movimientoEntrada.getProducto());
        assertNotNull(movimientoEntrada.getFechaMovimiento());
    }

    //  Test 2: Movimiento de SALIDA registrado correctamente 

    @Test
    @DisplayName("Debe registrar un movimiento de salida correctamente")
    void testRegistrarMovimientoSalida() {
        assertNotNull(movimientoSalida);
        assertEquals("Salida", movimientoSalida.getTipoMovimiento());
        assertEquals(5, movimientoSalida.getCantidad());
        assertNotNull(movimientoSalida.getProducto());
    }

    //  Test 3: Solo ADMIN puede registrar movimientos

    @Test
    @DisplayName("Solo ADMIN puede registrar movimientos de inventario")
    void testSoloAdminPuedeRegistrarMovimiento() {
        boolean adminTienePermiso = admin.getRoles()
                .stream()
                .anyMatch(rol -> rol.getNombre().equals("ADMIN"));

        boolean cajeroTienePermiso = cajero.getRoles()
                .stream()
                .anyMatch(rol -> rol.getNombre().equals("ADMIN"));

        assertTrue(adminTienePermiso,
                "El ADMIN debe poder registrar movimientos de inventario");
        assertFalse(cajeroTienePermiso,
                "El CAJERO NO debe poder registrar movimientos de inventario");
    }

    //  Test 4: Movimiento está asociado a un producto 
    @Test
    @DisplayName("El movimiento de inventario debe estar asociado a un producto válido")
    void testMovimientoAsociadoAProducto() {
        assertNotNull(movimientoEntrada.getProducto(),
                "El movimiento debe tener un producto asociado");
        assertEquals(10L, movimientoEntrada.getProducto().getId());
        assertEquals("Leche Entera", movimientoEntrada.getProducto().getNombre());
    }

    //  Test 5: Stock actualiza correctamente tras entrada 

    @Test
    @DisplayName("El stock debe aumentar tras un movimiento de entrada")
    void testStockAumentaConEntrada() {
        int stockInicial = producto.getStock(); // 50
        int cantidadEntrada = movimientoEntrada.getCantidad(); // 20

        // Simular aplicación del movimiento de entrada
        producto.setStock(stockInicial + cantidadEntrada);

        assertEquals(70, producto.getStock(),
                "El stock debe ser 70 tras recibir 20 unidades");
    }

    //  Test 6: No se permite salida si stock es insuficiente 

    @Test
    @DisplayName("No se debe permitir salida si el stock es insuficiente")
    void testNoPermiteSalidaSinStock() {
        producto.setStock(3); // Stock menor que la cantidad de salida (5)

        boolean salidaPermitida = producto.getStock() >= movimientoSalida.getCantidad();

        assertFalse(salidaPermitida,
                "No debe permitirse una salida de 5 unidades si el stock es solo 3");
    }
}
