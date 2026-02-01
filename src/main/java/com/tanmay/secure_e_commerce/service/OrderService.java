package com.tanmay.secure_e_commerce.service;

import com.tanmay.secure_e_commerce.dto.OrderDTO;
import com.tanmay.secure_e_commerce.dto.OrderItemDTO;
import com.tanmay.secure_e_commerce.entity.Order;
import com.tanmay.secure_e_commerce.entity.OrderItem;
import com.tanmay.secure_e_commerce.entity.Product;
import com.tanmay.secure_e_commerce.entity.User;
import com.tanmay.secure_e_commerce.enums.OrderStatus;
import com.tanmay.secure_e_commerce.enums.Role;
import com.tanmay.secure_e_commerce.exception.ForbiddenException;
import com.tanmay.secure_e_commerce.exception.ResourceNotFoundException;
import com.tanmay.secure_e_commerce.repository.OrderRepository;
import com.tanmay.secure_e_commerce.repository.ProductRepository;
import com.tanmay.secure_e_commerce.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userDetailsService.getUserByUsername(username);
    }

    private void validateCustomerRole() {
        User user = getCurrentUser();
        if (user.getRole() != Role.CUSTOMER) {
            throw new ForbiddenException("Only CUSTOMER users can perform this operation");
        }
    }

    private void validateAdminRole() {
        User user = getCurrentUser();
        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only ADMIN users can perform this operation");
        }
    }

    @Transactional
    public OrderDTO placeOrder(OrderDTO orderDTO) {
        validateCustomerRole();

        User user = getCurrentUser();

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CREATED);

        // order
        List<OrderItem> orderItems = orderDTO.getOrderItems()
                .stream()
                .map(itemDTO -> {
                    Product product = productRepository.findById(itemDTO.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDTO.getProductId()));

                    // stock availability
                    if (product.getStock() < itemDTO.getQuantity()) {
                        throw new IllegalArgumentException("Insufficient stock for : " + product.getName());
                    }

                    // Reduce stock
                    product.setStock(product.getStock() - itemDTO.getQuantity());
                    productRepository.save(product);

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProduct(product);
                    orderItem.setQuantity(itemDTO.getQuantity());
                    orderItem.setPrice(product.getPrice());

                    return orderItem;
                })
                .collect(Collectors.toList());

        // calculation
        BigDecimal totalAmount = orderItems
                .stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }

    //get
    public List<OrderDTO> getMyOrders() {
        validateCustomerRole();

        User user = getCurrentUser();
        return orderRepository.findByUserId(user.getId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getAllOrders() {
        validateAdminRole();

        return orderRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long id) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Customer limit
        if (user.getRole() == Role.CUSTOMER && !order.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only view your own orders");
        }

        return convertToDTO(order);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long id, OrderStatus newStatus) {
        validateAdminRole();

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Validate
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        return convertToDTO(updatedOrder);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot update status of cancelled order");
        }

        if (currentStatus == OrderStatus.CONFIRMED && newStatus == OrderStatus.CREATED) {
            throw new IllegalArgumentException("Cannot reverse to previous status");
        }
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setUsername(order.getUser().getUsername());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());

        List<OrderItemDTO> itemDTOs = order.getOrderItems()
                .stream()
                .map(item -> {
                    OrderItemDTO itemDTO = new OrderItemDTO();
                    itemDTO.setId(item.getId());
                    itemDTO.setProductId(item.getProduct().getId());
                    itemDTO.setProductName(item.getProduct().getName());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setPrice(item.getPrice());
                    return itemDTO;
                })
                .collect(Collectors.toList());

        dto.setOrderItems(itemDTOs);
        return dto;
    }
}