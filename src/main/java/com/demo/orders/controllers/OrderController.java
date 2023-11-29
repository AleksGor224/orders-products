package com.demo.orders.controllers;

import com.demo.orders.CommonUtils;
import com.demo.orders.dtos.OrderDto;
import com.demo.orders.dtos.OrderViewDto;
import com.demo.orders.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Create a new order")
    @ApiResponse(responseCode = "201", description = "Submitted",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrderDto.class)))
    public ResponseEntity<String> createOrder(@RequestBody OrderDto order) {
        String orderId = orderService.createOrder(order);
        return new ResponseEntity<>(orderId, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order by id")
    @ApiResponse(responseCode = "200", description = "Order was found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrderViewDto.class)))
    public ResponseEntity<OrderViewDto> getOrderById(@PathVariable String id) {
        CommonUtils.validateUUID(id, "id");
        OrderViewDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @Operation(summary = "Get all orders")
    @ApiResponse(responseCode = "200", description = "Successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrderViewDto.class)))
    public ResponseEntity<List<OrderViewDto>> getAllOrders() {
        List<OrderViewDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an order by id")
    @ApiResponse(responseCode = "200", description = "Submitted",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrderViewDto.class)))
    public ResponseEntity<String> updateOrder(@PathVariable String id, @RequestBody OrderDto order) {
        CommonUtils.validateUUID(id, "id");
        String orderId = orderService.updateOrder(id, order);
        return ResponseEntity.ok(orderId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove an order by id")
    @ApiResponse(responseCode = "204", description = "Submitted")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        CommonUtils.validateUUID(id, "id");
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderId}/products/{productId}")
    @Operation(summary = "Add a product to the order")
    @ApiResponse(responseCode = "200", description = "Submitted",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrderViewDto.class)))
    public ResponseEntity<Void> addProductToOrder(@PathVariable String orderId, @PathVariable String productId) {
        CommonUtils.validateUUID(orderId, "orderId");
        CommonUtils.validateUUID(productId, "productId");
        orderService.addProductToOrder(orderId, productId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{orderId}/products/{productId}")
    @Operation(summary = "Delete a product from the order")
    @ApiResponse(responseCode = "200", description = "Submitted",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrderViewDto.class)))
    public ResponseEntity<Void> removeProductFromOrder(@PathVariable String orderId, @PathVariable String productId) {
        CommonUtils.validateUUID(orderId, "orderId");
        CommonUtils.validateUUID(productId, "productId");
        orderService.removeProductFromOrder(orderId, productId);
        return ResponseEntity.ok().build();
    }
}