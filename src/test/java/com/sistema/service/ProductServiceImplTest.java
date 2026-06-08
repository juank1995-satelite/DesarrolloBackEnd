package com.sistema.service;

import com.sistema.exception.InsufficientStockException;
import com.sistema.exception.ProductNotFoundException;
import com.sistema.model.Product;
import com.sistema.repository.ProductRepository;
import com.sistema.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Producto Mockeado");
        testProduct.setDescription("Descripción de prueba");
        testProduct.setPrice(new BigDecimal("100.00"));
        testProduct.setStock(10);
        testProduct.setActive(true);
    }

    @Test
    @DisplayName("Mockito: Buscar producto por ID exitoso")
    void testFindByIdSuccess() {
        // Configuramos el Mock: Cuando el repositorio busque el ID 1, devuelve nuestro testProduct
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(testProduct));

        Product found = productService.findById(1L);

        assertNotNull(found);
        assertEquals("Producto Mockeado", found.getName());
        verify(productRepository, times(1)).findByIdAndActiveTrue(1L);
    }

    @Test
    @DisplayName("Mockito: Buscar producto por ID falla (No Encontrado)")
    void testFindByIdNotFound() {
        // Configuramos el Mock para devolver vacío
        when(productRepository.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.findById(99L));
        verify(productRepository, times(1)).findByIdAndActiveTrue(99L);
    }

    @Test
    @DisplayName("Mockito: Actualizar stock exitosamente")
    void testUpdateStockSuccess() {
        // Simulamos que encuentra el producto
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(testProduct));
        // Simulamos el guardado
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product updated = productService.updateStock(1L, -5); // Restamos 5 al stock de 10

        assertEquals(5, updated.getStock());
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    @DisplayName("Mockito: Actualizar stock falla por stock insuficiente")
    void testUpdateStockInsufficient() {
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(testProduct));

        // Intentamos restar 15 cuando solo hay 10
        assertThrows(InsufficientStockException.class, () -> productService.updateStock(1L, -15));
        
        // Verificamos que NUNCA se llamó al método save porque falló antes
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Mockito: Borrado lógico exitoso")
    void testDeleteLogically() {
        when(productRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        productService.deleteLogically(1L);

        // Verificamos que se haya cambiado el estado a false
        assertFalse(testProduct.getActive());
        verify(productRepository, times(1)).save(testProduct);
    }
}
