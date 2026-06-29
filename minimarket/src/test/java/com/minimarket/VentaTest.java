package com.minimarket;

import com.minimarket.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la entidad Venta.
 *
 * Valida que:
 *  - Solo usuarios con rol CAJERO (o ADMIN) pueden generar ventas.
 *  - Una venta refleja correctamente los productos vendidos y sus precios.
 *  - No se puede generar una venta si el stock es insuficiente.
 *  - La venta queda registrada con fecha y usuario asociado.
 */
@DisplayName("Pruebas Unitarias - Venta")
public class VentaTest {

    private Venta venta;
    private Producto producto1;
    private Producto producto2;
    private Usuario cajero;
    private Usuario cliente;
    private DetalleVenta detalle1;
    private DetalleVenta detalle2;

    @BeforeEach
    void setUp() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Snacks");

        producto1 = new Producto();
        producto1.setId(1L);
        producto1.setNombre("Papas Fritas");
        producto1.setPrecio(1200.0);
        producto1.setStock(30);
        producto1.setCategoria(categoria);

        producto2 = new Producto();
        producto2.setId(2L);
        producto2.setNombre("Refresco Cola");
        producto2.setPrecio(800.0);
        producto2.setStock(20);
        producto2.setCategoria(categoria);

        // Usuario CAJERO
        cajero = new Usuario();
        cajero.setId(3L);
        cajero.setUsername("cajero1");
        cajero.setPassword("cajero123");
        cajero.setRoles(Set.of(new Rol("CAJERO")));

        // Usuario CLIENTE (sin permiso de generar ventas)
        cliente = new Usuario();
        cliente.setId(2L);
        cliente.setUsername("cliente1");
        cliente.setPassword("cliente123");
        cliente.setRoles(Set.of(new Rol("CLIENTE")));

        // Detalles de venta
        detalle1 = new DetalleVenta();
        detalle1.setId(1L);
        detalle1.setProducto(producto1);
        detalle1.setCantidad(2);
        detalle1.setPrecio(producto1.getPrecio());

        detalle2 = new DetalleVenta();
        detalle2.setId(2L);
        detalle2.setProducto(producto2);
        detalle2.setCantidad(3);
        detalle2.setPrecio(producto2.getPrecio());

        // Venta principal
        venta = new Venta();
        venta.setId(1L);
        venta.setUsuario(cajero);
        venta.setFecha(new Date());
        venta.setDetalles(new ArrayList<>(List.of(detalle1, detalle2)));
    }

    // ─── Test 1: Venta creada con datos correctos ─────────────────────────────

    @Test
    @DisplayName("Debe crear una venta con todos sus datos correctamente")
    void testCrearVenta() {
        assertNotNull(venta);
        assertNotNull(venta.getFecha());
        assertNotNull(venta.getUsuario());
        assertEquals(2, venta.getDetalles().size());
    }

    // ─── Test 2: Solo CAJERO puede generar ventas ─────────────────────────────

    @Test
    @DisplayName("Solo usuarios con rol CAJERO o ADMIN pueden generar ventas")
    void testSoloCajeroPuedeGenerarVenta() {
        boolean cajeroTienePermiso = cajero.getRoles()
                .stream()
                .anyMatch(rol -> rol.getNombre().equals("CAJERO")
                              || rol.getNombre().equals("ADMIN"));

        boolean clienteTienePermiso = cliente.getRoles()
                .stream()
                .anyMatch(rol -> rol.getNombre().equals("CAJERO")
                              || rol.getNombre().equals("ADMIN"));

        assertTrue(cajeroTienePermiso,
                "El CAJERO debe poder generar ventas");
        assertFalse(clienteTienePermiso,
                "El CLIENTE no debe poder generar ventas");
    }

    // ─── Test 3: La venta refleja correctamente los productos ─────────────────

    @Test
    @DisplayName("La venta debe reflejar correctamente los productos vendidos")
    void testVentaReflEjaProductos() {
        List<DetalleVenta> detalles = venta.getDetalles();

        assertNotNull(detalles);
        assertEquals(2, detalles.size());

        DetalleVenta primerDetalle = detalles.get(0);
        assertEquals("Papas Fritas", primerDetalle.getProducto().getNombre());
        assertEquals(2, primerDetalle.getCantidad());
        assertEquals(1200.0, primerDetalle.getPrecio());
    }

    // ─── Test 4: Cálculo correcto del total de la venta ──────────────────────

    @Test
    @DisplayName("El total de la venta debe calcularse correctamente")
    void testTotalVentaCorrectoCalculado() {
        double totalEsperado = (detalle1.getCantidad() * detalle1.getPrecio())
                             + (detalle2.getCantidad() * detalle2.getPrecio());
        // 2 * 1200 + 3 * 800 = 2400 + 2400 = 4800

        double totalCalculado = venta.getDetalles().stream()
                .mapToDouble(d -> d.getCantidad() * d.getPrecio())
                .sum();

        assertEquals(4800.0, totalCalculado, 0.01,
                "El total de la venta debe ser $4800");
        assertEquals(totalEsperado, totalCalculado, 0.01);
    }
    //  Test 5: No se genera venta si no hay stock suficiente 

    @Test
    @DisplayName("No debe generarse venta si algún producto no tiene stock suficiente")
    void testNoGeneraVentaSinStock() {
        producto1.setStock(1); // Solo 1 unidad, pero se quieren comprar 2

        boolean stockSuficiente = venta.getDetalles().stream()
                .allMatch(d -> d.getProducto().getStock() >= d.getCantidad());

        assertFalse(stockSuficiente,
                "La venta no debe generarse si algún producto no tiene stock suficiente");
    }

    //  Test 6: Venta está asociada al cajero correcto 

    @Test
    @DisplayName("La venta debe estar asociada al usuario cajero que la generó")
    void testVentaAsociadaACajero() {
        assertNotNull(venta.getUsuario());
        assertEquals("cajero1", venta.getUsuario().getUsername());
        assertTrue(venta.getUsuario().getRoles()
                        .stream()
                        .anyMatch(rol -> rol.getNombre().equals("CAJERO")));
    }
}
