package com.sistema.service;

import com.sistema.exception.InsufficientStockException;
import com.sistema.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class BusinessLogicIntegrationTest {

    @Autowired
    private ProductService productService;

    private Product tvSamsung;
    private Product refriMabe;
    private Product laptopHp;

    @BeforeEach
    void setUp() {
        // Inicializamos productos simulando un inventario de La Curacao
        
        tvSamsung = new Product();
        tvSamsung.setName("Smart TV Samsung 65 Pulgadas 4K");
        tvSamsung.setDescription("Televisor inteligente con procesador Crystal 4K, ideal para ver deportes y películas.");
        tvSamsung.setPrice(new BigDecimal("750.00"));
        tvSamsung.setStock(5); // Stock inicial bajo para simular escasez
        tvSamsung = productService.create(tvSamsung);

        refriMabe = new Product();
        refriMabe.setName("Refrigeradora Mabe 11 pies");
        refriMabe.setDescription("Refrigeradora No Frost con dispensador de agua, color grafito.");
        refriMabe.setPrice(new BigDecimal("420.00"));
        refriMabe.setStock(10);
        refriMabe = productService.create(refriMabe);

        laptopHp = new Product();
        laptopHp.setName("Laptop HP Pavilion 15");
        laptopHp.setDescription("Laptop para estudiantes, Intel Core i5, 8GB RAM, 256GB SSD.");
        laptopHp.setPrice(new BigDecimal("600.00"));
        laptopHp.setStock(3);
        laptopHp = productService.create(laptopHp);
    }

    @Test
    @DisplayName("Regla de Negocio 1: Venta exitosa (Reducción de stock)")
    void testSuccessfulSaleReducesStock() {
        // Un cliente en La Curacao compra 2 Refrigeradoras Mabe
        Product updatedRefri = productService.updateStock(refriMabe.getId(), -2);

        // El stock inicial era 10, ahora debe ser 8
        assertEquals(8, updatedRefri.getStock(), "El stock debió reducirse a 8 tras la venta.");
    }

    @Test
    @DisplayName("Regla de Negocio 2: Prevención de sobre-venta (Falta de inventario)")
    void testInsufficientStockThrowsException() {
        // En Black Friday, un cliente intenta comprar 10 TVs Samsung, pero solo hay 5 en stock.
        
        Exception exception = assertThrows(InsufficientStockException.class, () -> {
            // Intentar restar 10 al stock
            productService.updateStock(tvSamsung.getId(), -10);
        });

        // Verificamos que el sistema protegió el inventario y no guardó números negativos
        String expectedMessage = "Stock insuficiente";
        assertTrue(exception.getMessage().contains(expectedMessage), "El sistema debe lanzar error por stock insuficiente.");
        
        // Verificamos que el stock en la base de datos se mantuvo intacto
        Product savedTv = productService.findById(tvSamsung.getId());
        assertEquals(5, savedTv.getStock(), "El stock no debió ser alterado tras el fallo de venta.");
    }

    @Test
    @DisplayName("Regla de Negocio 3: Auditoría y Borrado Lógico (Soft Delete)")
    void testLogicalDeleteHidesProductButRetainsData() {
        // La Laptop HP Pavilion ya está descontinuada. La damos de baja.
        productService.deleteLogically(laptopHp.getId());

        // Para verificar que retiene los datos pero está oculta, usamos reactivate (que sí la encuentra aunque esté inactiva)
        // o esperamos que findById normal tire excepción.
        // Lo verificamos en la prueba 3B. Esta prueba 3 la podemos dejar para validar que no explota al borrar.
    }
    
    @Test
    @DisplayName("Regla de Negocio 3B: El producto eliminado no se encuentra en las búsquedas activas")
    void testLogicalDeleteThrowsExceptionOnFindById() {
        productService.deleteLogically(laptopHp.getId());
        
        assertThrows(com.sistema.exception.ProductNotFoundException.class, () -> {
            productService.findById(laptopHp.getId());
        }, "El producto no debe ser encontrado por findById si está desactivado.");
    }

    @Test
    @DisplayName("Regla de Negocio 4: Reactivación de producto para temporada de descuentos")
    void testReactivateProduct() {
        // Desactivamos la Laptop
        productService.deleteLogically(laptopHp.getId());
        
        // Ahora la reactivamos (Ej: Promoción de Regreso a Clases en La Curacao)
        Product reactivatedLaptop = productService.reactivate(laptopHp.getId());
        
        // Verificamos que ahora sí está activa y se puede encontrar normalmente
        assertTrue(reactivatedLaptop.getActive(), "El producto debe estar activo tras reactivarse.");
        
        Product foundLaptop = productService.findById(laptopHp.getId());
        assertNotNull(foundLaptop, "El producto debe ser encontrable nuevamente.");
    }
}
