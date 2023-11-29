package com.demo.orders.controllers;

import com.demo.orders.dtos.ProductDto;
import com.demo.orders.dtos.ProductViewDto;
import com.demo.orders.services.ProductsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductsService productsService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createProductTest() throws Exception {
        ProductDto productDto = new ProductDto();
        String productId = UUID.randomUUID().toString();
        given(productsService.createProduct(any(ProductDto.class))).willReturn(productId);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", is(productId)));
    }

    @Test
    void getProductByIdTest() throws Exception {
        String id = UUID.randomUUID().toString();
        ProductViewDto productViewDto = new ProductViewDto();
        given(productsService.getProductById(eq(id))).willReturn(productViewDto);

        mockMvc.perform(get("/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(productViewDto)));
    }

    @Test
    void updateProductTest() throws Exception {
        String id = UUID.randomUUID().toString();
        ProductDto productDto = new ProductDto();
        String productId = UUID.randomUUID().toString();
        given(productsService.updateProduct(eq(id), any(ProductDto.class))).willReturn(productId);

        mockMvc.perform(put("/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(productId)));
    }

    @Test
    void deleteProductTest() throws Exception {
        String id = UUID.randomUUID().toString();

        mockMvc.perform(delete("/products/{id}", id))
                .andExpect(status().isNoContent());
    }
}