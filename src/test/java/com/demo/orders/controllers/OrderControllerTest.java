package com.demo.orders.controllers;

import com.demo.orders.dtos.OrderDto;
import com.demo.orders.dtos.OrderViewDto;
import com.demo.orders.services.OrderService;
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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createOrderTest() throws Exception {
        OrderDto orderDto = new OrderDto();
        String orderId = UUID.randomUUID().toString();
        given(orderService.createOrder(any(OrderDto.class))).willReturn(orderId);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", is(orderId)));
    }

    @Test
    void getOrderByIdTest() throws Exception {
        String id = UUID.randomUUID().toString();
        OrderViewDto orderViewDto = new OrderViewDto();
        given(orderService.getOrderById(eq(id))).willReturn(orderViewDto);

        mockMvc.perform(get("/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(orderViewDto)));
    }

    @Test
    void getAllOrdersTest() throws Exception {
        List<OrderViewDto> orders = Arrays.asList(new OrderViewDto(), new OrderViewDto());
        given(orderService.getAllOrders()).willReturn(orders);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(orders)));
    }

    @Test
    void updateOrderTest() throws Exception {
        String id = UUID.randomUUID().toString();
        OrderDto orderDto = new OrderDto();
        String orderId = UUID.randomUUID().toString();
        given(orderService.updateOrder(eq(id), any(OrderDto.class))).willReturn(orderId);

        mockMvc.perform(put("/orders/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(orderId)));
    }

    @Test
    void deleteOrderTest() throws Exception {
        String id = UUID.randomUUID().toString();

        mockMvc.perform(delete("/orders/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void addProductToOrderTest() throws Exception {
        String orderId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();

        mockMvc.perform(post("/orders/{orderId}/products/{productId}", orderId, productId))
                .andExpect(status().isOk());
    }

    @Test
    void removeProductFromOrderTest() throws Exception {
        String orderId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();

        mockMvc.perform(delete("/orders/{orderId}/products/{productId}", orderId, productId))
                .andExpect(status().isOk());
    }
}