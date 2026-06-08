package com.sistema.controller;

import com.sistema.dto.ProductDTO;
import com.sistema.dto.StockUpdateDTO;
import com.sistema.mapper.ProductMapper;
import com.sistema.model.Product;
import com.sistema.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    /**
     * Listar todos los productos (Paginado)
     */
    @GetMapping
    public ResponseEntity<java.util.Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<Product> productsPage = productService.findAllActive(PageRequest.of(page, size));
        List<ProductDTO> dtos = productsPage.getContent().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
        
        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("totalElements", productsPage.getTotalElements());
        response.put("totalPages", productsPage.getTotalPages());
        response.put("size", productsPage.getSize());
        response.put("content", dtos);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Consultar un Producto por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Product product = productService.findById(id);
        return ResponseEntity.ok(productMapper.toDto(product));
    }

    /**
     * Actualizar el Stock de un producto
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductDTO> updateStock(
            @PathVariable Long id, 
            @RequestBody StockUpdateDTO stockUpdate) {
        
        Product updatedProduct = productService.updateStock(id, stockUpdate.getQuantityChange());
        return ResponseEntity.ok(productMapper.toDto(updatedProduct));
    }

    /**
     * Crear un nuevo Producto
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        Product entity = productMapper.toEntity(productDTO);
        Product created = productService.create(entity);
        return ResponseEntity.status(201).body(productMapper.toDto(created));
    }

    /**
     * Actualizar un Producto existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody ProductDTO productDTO) {
        
        Product entity = productMapper.toEntity(productDTO);
        entity.setId(id); // Asegurarnos de que el ID coincida con la ruta
        Product updated = productService.update(entity);
        return ResponseEntity.ok(productMapper.toDto(updated));
    }

    /**
     * Eliminar (Lógicamente) un Producto
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteLogically(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivar un Producto eliminado lógicamente
     */
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<ProductDTO> reactivateProduct(@PathVariable Long id) {
        Product reactivated = productService.reactivate(id);
        return ResponseEntity.ok(productMapper.toDto(reactivated));
    }
}
