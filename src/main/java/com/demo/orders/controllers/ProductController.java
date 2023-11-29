package com.demo.orders.controllers;

import com.demo.orders.CommonUtils;
import com.demo.orders.dtos.ProductDto;
import com.demo.orders.dtos.ProductViewDto;
import com.demo.orders.services.ProductsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductsService productsService;

    @Autowired
    public ProductController(ProductsService productsService) {
        this.productsService = productsService;
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    @ApiResponse(responseCode = "201", description = "The Product created successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProductViewDto.class)))
    public ResponseEntity<String> createProduct(@RequestBody ProductDto product) {
        String productId = productsService.createProduct(product);
        return new ResponseEntity<>(productId, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    @ApiResponse(responseCode = "200", description = "Product was found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProductViewDto.class)))
    public ResponseEntity<ProductViewDto> getProductById(@PathVariable String id) {
        CommonUtils.validateUUID(id, "id");
        ProductViewDto product = productsService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product by ID")
    @ApiResponse(responseCode = "200", description = "Product updated successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProductViewDto.class)))
    public ResponseEntity<String> updateProduct(@PathVariable String id, @RequestBody ProductDto product) {
        CommonUtils.validateUUID(id, "id");
        String productId = productsService.updateProduct(id, product);
        return ResponseEntity.ok(productId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product by id")
    @ApiResponse(responseCode = "204", description = "Product deleted successfully")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        CommonUtils.validateUUID(id, "id");
        productsService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}