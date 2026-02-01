package com.tanmay.secure_e_commerce.controller;

import com.tanmay.secure_e_commerce.dto.OrderDTO;
import com.tanmay.secure_e_commerce.enums.OrderStatus;
import com.tanmay.secure_e_commerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO> placeOrder(@Valid @RequestBody OrderDTO orderDTO) {
        OrderDTO created = orderService.placeOrder(orderDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderDTO>> getMyOrders() {
        List<OrderDTO> orders = orderService.getMyOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        OrderStatus newStatus = OrderStatus.valueOf(statusUpdate.get("status"));
        OrderDTO updated = orderService.updateOrderStatus(id, newStatus);
        return ResponseEntity.ok(updated);
    }
}