package com.messismo.bar.Services;

import com.messismo.bar.DTOs.*;
import com.messismo.bar.Entities.*;
import com.messismo.bar.Exceptions.*;
import com.messismo.bar.Repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final ProductOrderRepository productOrderRepository;
    
    private final PointsService pointsService;
    
    private final BenefitService benefitService;
    
    private final BenefitRepository benefitRepository;

    public String addNewOrder(OrderRequestDTO orderRequestDTO) throws Exception {
        try {
            User employee = userRepository.findByEmail(orderRequestDTO.getRegisteredEmployeeEmail()).orElseThrow(() -> new UserNotFoundException("No user has that email"));
            
            // Validar clientId si se proporciona
            if (orderRequestDTO.getClientId() != null) {
                userRepository.findByClientId(String.valueOf(orderRequestDTO.getClientId()))
                    .orElseThrow(() -> new ClientIdNotFoundException("Client ID " + orderRequestDTO.getClientId() + " not found"));
            }
            
            NewProductOrderListDTO newProductOrderListDTO = createProductOrder(orderRequestDTO.getProductOrders());
            Order newOrder = new Order(employee, orderRequestDTO.getDateCreated(), newProductOrderListDTO.getProductOrderList(), newProductOrderListDTO.getTotalPrice(), newProductOrderListDTO.getTotalCost(), orderRequestDTO.getClientId());
            orderRepository.save(newOrder);
            return "Order created successfully";
        } catch (UserNotFoundException | ProductQuantityBelowAvailableStock | ClientIdNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("CANNOT create an order at the moment");
        }
    }

    public String closeOrder(OrderIdDTO orderIdDTO) throws Exception {
        try {
            Order order = orderRepository.findById(orderIdDTO.getOrderId()).orElseThrow(() -> new OrderNotFoundException("Order not found"));
            order.close();
            orderRepository.save(order);
            return "Order closed successfully";
        } catch (OrderNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("CANNOT close an order at the moment");
        }
    }

    public String closeOrderWithClient(CloseOrderDTO closeOrderDTO) throws Exception {
        try {
            Order order = orderRepository.findById(closeOrderDTO.getOrderId()).orElseThrow(() -> new OrderNotFoundException("Order not found"));
            
            // Validar clientId si se proporciona
            User client = null;
            if (closeOrderDTO.getClientId() != null) {
                client = userRepository.findByClientId(String.valueOf(closeOrderDTO.getClientId()))
                    .orElseThrow(() -> new ClientIdNotFoundException("Client ID " + closeOrderDTO.getClientId() + " not found"));
                order.setClientId(closeOrderDTO.getClientId());
            }
            
            // Aplicar beneficio si se proporciona y es válido
            Benefit appliedBenefit = null;
            double originalTotalPrice = order.getTotalPrice();
            double finalTotalPrice = originalTotalPrice;
            int pointsToUse = 0;
            
            if (closeOrderDTO.getBenefitId() != null && client != null) {
                appliedBenefit = benefitRepository.findById(closeOrderDTO.getBenefitId())
                    .orElseThrow(() -> new RuntimeException("Benefit not found"));
                
                // Validar que el cliente tenga suficientes puntos
                Double clientPoints = pointsService.getPointsAccount(String.valueOf(client.getClientId()))
                    .map(account -> account.getCurrentBalance())
                    .orElse(0.0);
                    
                if (clientPoints < appliedBenefit.getPointsRequired()) {
                    throw new RuntimeException("Insufficient points for this benefit");
                }
                
                // Validar que el beneficio aplique para el día actual
                String currentDay = java.time.LocalDate.now().getDayOfWeek().name();
                if (!appliedBenefit.isApplicableOnDay(currentDay)) {
                    throw new RuntimeException("This benefit is not available today");
                }
                
                // Validar beneficios de producto gratis
                if (appliedBenefit.getType() == Benefit.BenefitType.FREE_PRODUCT) {
                    if (!validateFreeProductBenefit(order, appliedBenefit)) {
                        throw new RuntimeException("This order does not contain the required product for this benefit");
                    }
                }
                
                // Calcular descuento según tipo de beneficio
                finalTotalPrice = calculateDiscountedPrice(order, appliedBenefit);
                pointsToUse = appliedBenefit.getPointsRequired();
                
                order.setAppliedBenefit(appliedBenefit);
                order.setPointsUsed(pointsToUse);
                
                // Actualizar total de la orden
                order.setTotalPrice(finalTotalPrice);
            }
            
            order.close();
            Order savedOrder = orderRepository.save(order);
            
            // Calcular y asignar puntos ganados (basado en el precio final)
            if (savedOrder.getClientId() != null) {
                Double pointsEarned = pointsService.addPointsForOrder(
                    String.valueOf(savedOrder.getClientId()), 
                    savedOrder.getTotalPrice(), 
                    savedOrder.getId()
                );
                savedOrder.setPointsEarned(pointsEarned);
                
                // Descontar puntos usados del beneficio
                if (pointsToUse > 0) {
                    pointsService.usePointsForBenefit(
                        String.valueOf(savedOrder.getClientId()),
                        pointsToUse
                    );
                }
                
                orderRepository.save(savedOrder);
            }
            
            return "Order closed successfully";
        } catch (OrderNotFoundException | ClientIdNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("CANNOT close an order at the moment: " + e.getMessage());
        }
    }

    public String modifyOrder(ModifyOrderDTO modifyOrderDTO) throws Exception {
        try {
            Order order = orderRepository.findById(modifyOrderDTO.getOrderId()).orElseThrow(() -> new OrderNotFoundException("Order not found"));
            NewProductOrderListDTO newProductOrderListDTO = createProductOrder(modifyOrderDTO.getProductOrders());
            order.updateProductOrders(newProductOrderListDTO.getProductOrderList());
            order.updateTotalPrice(newProductOrderListDTO.getTotalPrice());
            order.updateTotalCost(newProductOrderListDTO.getTotalCost());
            orderRepository.save(order);
            return "Order modified successfully";
        } catch (ProductQuantityBelowAvailableStock | OrderNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("CANNOT modify this order at the moment");
        }
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getAllOrdersBetweenTwoDates(Date startingDate, Date endingDate) {
        List<Order> allOrders = orderRepository.findAll();
        List<Order> filteredOrderByDate = new ArrayList<>();
        for (Order order : allOrders) {
            if (order.getDateCreated().after(startingDate) && order.getDateCreated().before(endingDate)) {
                filteredOrderByDate.add(order);
            }
        }
        return filteredOrderByDate;
    }

    public NewProductOrderListDTO createProductOrder(List<ProductOrderDTO> productOrderDTOList) throws ProductQuantityBelowAvailableStock {
        List<ProductOrder> productOrderList = new ArrayList<>();
        double totalPrice = 0.00;
        double totalCost = 0.00;
        for (ProductOrderDTO productOrderDTO : productOrderDTOList) {
            if (productOrderDTO.getProduct().getStock() < productOrderDTO.getQuantity()) {
                throw new ProductQuantityBelowAvailableStock("Not enough stock of a product");
            } else {
                Product product = productOrderDTO.getProduct();
                product.removeStock(productOrderDTO.getQuantity());
                productRepository.save(product);
                totalPrice += (product.getUnitPrice() * productOrderDTO.getQuantity());
                totalCost += (product.getUnitCost() * productOrderDTO.getQuantity());
                ProductOrder productOrder = new ProductOrder(product.getName(), product.getUnitPrice(), product.getUnitCost(), product.getCategory(), productOrderDTO.getQuantity());
                productOrderRepository.save(productOrder);
                productOrderList.add(productOrder);
            }
        }
        return NewProductOrderListDTO.builder().productOrderList(productOrderList).totalCost(totalCost).totalPrice(totalPrice).build();
    }

    public List<Order> getOrdersByClientEmail(String email) throws Exception {
        try {
            // Obtener el usuario por email
            User client = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Client not found"));
            
            // Obtener órdenes por clientId
            return orderRepository.findByClientId(Long.parseLong(client.getClientId()));
        } catch (Exception e) {
            throw new Exception("Error retrieving client orders: " + e.getMessage());
        }
    }
    
    private double calculateDiscountedPrice(Order order, Benefit benefit) {
        double originalPrice = order.getTotalPrice();
        
        switch (benefit.getType()) {
            case DISCOUNT:
                return calculateDiscountAmount(originalPrice, benefit);
            case FREE_PRODUCT:
                return calculateFreeProductDiscount(order, benefit);
            default:
                return originalPrice;
        }
    }
    
    private double calculateDiscountAmount(double originalPrice, Benefit benefit) {
        switch (benefit.getDiscountType()) {
            case PERCENTAGE:
                double discountAmount = originalPrice * (benefit.getDiscountValue() / 100.0);
                return Math.max(0, originalPrice - discountAmount);
            case FIXED_AMOUNT:
                return Math.max(0, originalPrice - benefit.getDiscountValue());
            default:
                return originalPrice;
        }
    }
    
    private double calculateFreeProductDiscount(Order order, Benefit benefit) {
        double originalPrice = order.getTotalPrice();
        
        try {
            // Obtener la lista de product IDs del beneficio
            List<Long> productIds = jsonToLongList(benefit.getProductIds());
            if (productIds.isEmpty()) {
                return originalPrice;
            }
            
            // Obtener el primer producto del beneficio
            Long benefitProductId = productIds.get(0);
            Product benefitProduct = productRepository.findById(benefitProductId).orElse(null);
            
            if (benefitProduct == null) {
                return originalPrice;
            }
            
            // Buscar el producto en la orden por nombre
            for (ProductOrder productOrder : order.getProductOrders()) {
                if (productOrder.getProductName().equals(benefitProduct.getName())) {
                    // Descontar el precio de UNA unidad del producto
                    double productPrice = productOrder.getProductUnitPrice();
                    return Math.max(0, originalPrice - productPrice);
                }
            }
        } catch (Exception e) {
            // Si hay algún error, no aplicar descuento
            return originalPrice;
        }
        
        // Si el producto no está en la orden, no aplicar descuento
        return originalPrice;
    }
    
    // Validar si la orden contiene el producto requerido para el beneficio
    private boolean validateFreeProductBenefit(Order order, Benefit benefit) {
        try {
            List<Long> productIds = jsonToLongList(benefit.getProductIds());
            if (productIds.isEmpty()) {
                return false;
            }
            
            // Obtener el primer producto del beneficio
            Long benefitProductId = productIds.get(0);
            Product benefitProduct = productRepository.findById(benefitProductId).orElse(null);
            
            if (benefitProduct == null) {
                return false;
            }
            
            // Verificar si la orden contiene el producto
            for (ProductOrder productOrder : order.getProductOrders()) {
                if (productOrder.getProductName().equals(benefitProduct.getName())) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // Helper method para convertir JSON a List<Long>
    private List<Long> jsonToLongList(String json) {
        if (json == null || json.isEmpty()) return List.of();
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
