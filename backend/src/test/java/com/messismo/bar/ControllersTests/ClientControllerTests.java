package com.messismo.bar.ControllersTests;

import com.messismo.bar.Controllers.ClientController;
import com.messismo.bar.DTOs.ClientProfileDTO;
import com.messismo.bar.DTOs.ProductClientViewDTO;
import com.messismo.bar.DTOs.ProductDTO;
import com.messismo.bar.Entities.Category;
import com.messismo.bar.Entities.Order;
import com.messismo.bar.Entities.PointsTransaction;
import com.messismo.bar.Entities.TransactionType;
import com.messismo.bar.Entities.Product;
import com.messismo.bar.Services.OrderService;
import com.messismo.bar.Services.PointsService;
import com.messismo.bar.Services.ProductService;
import com.messismo.bar.Services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientControllerTests {

    @InjectMocks
    private ClientController clientController;

    @Mock
    private ProductService productService;

    @Mock
    private UserService userService;

    @Mock
    private OrderService orderService;

    @Mock
    private PointsService pointsService;

    @Mock
    private Authentication authentication;

    private ProductDTO sampleProduct;
    private ClientProfileDTO sampleClientProfile;
    private Order sampleOrder;
    private PointsTransaction sampleTransaction;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        sampleProduct = ProductDTO.builder()
                .name("Margherita Pizza")
                .description("Classic pizza with tomato and mozzarella")
                .unitPrice(15.99)
                .category("Main Course")
                .build();

        sampleClientProfile = ClientProfileDTO.builder()
                .currentPoints(150.0)
                .clientId("CLI001")
                .username("john_client")
                .email("john@client.com")
                .build();

        sampleOrder = new Order();
        sampleOrder.setId(1L);
        sampleOrder.setStatus("CLOSED");
        sampleOrder.setTotalPrice(25.99);
        sampleOrder.setClientId(1L);

        sampleTransaction = new PointsTransaction("CLI001", TransactionType.EARNED, 100.0, "ORDER", "Order completion");
        sampleTransaction.setId(1L);
    }

    @Test
    public void testGetProductsForClient_Success() {
        Category category = new Category();
        category.setName("Main Course");
        Product product = Product.builder()
                .productId(1L)
                .name("Margherita Pizza")
                .description("Classic pizza with tomato and mozzarella")
                .unitPrice(15.99)
                .category(category)
                .build();
        List<Product> products = Arrays.asList(product);
        when(productService.getAllProducts()).thenReturn(products);

        ResponseEntity<?> response = clientController.getProductsForClient();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof List);
        
        @SuppressWarnings("unchecked")
        List<ProductClientViewDTO> clientProducts = (List<ProductClientViewDTO>) response.getBody();
        assertEquals(1, clientProducts.size());
        assertEquals("Margherita Pizza", clientProducts.get(0).getName());
        assertEquals(15.99, clientProducts.get(0).getUnitPrice());
        
        verify(productService, times(1)).getAllProducts();
    }

    @Test
    public void testGetProductsForClient_ServiceException() {
        when(productService.getAllProducts()).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = clientController.getProductsForClient();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error retrieving products", response.getBody());
        
        verify(productService, times(1)).getAllProducts();
    }

    @Test
    public void testGetClientProfile_Success() throws Exception {
        String email = "john@client.com";
        when(authentication.getName()).thenReturn(email);
        when(userService.getClientProfile(email)).thenReturn(sampleClientProfile);

        ResponseEntity<?> response = clientController.getClientProfile(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleClientProfile, response.getBody());
        
        verify(authentication, times(1)).getName();
        verify(userService, times(1)).getClientProfile(email);
    }



    @Test
    public void testGetClientProfile_ServiceException() throws Exception {
        String email = "john@client.com";
        when(authentication.getName()).thenReturn(email);
        when(userService.getClientProfile(email)).thenThrow(new Exception("User not found"));

        ResponseEntity<?> response = clientController.getClientProfile(authentication);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error retrieving profile", response.getBody());
        
        verify(authentication, times(1)).getName();
        verify(userService, times(1)).getClientProfile(email);
    }

    @Test
    public void testGetClientOrders_Success() throws Exception {
        String email = "john@client.com";
        List<Order> orders = Arrays.asList(sampleOrder);
        when(authentication.getName()).thenReturn(email);
        when(orderService.getOrdersByClientEmail(email)).thenReturn(orders);

        ResponseEntity<?> response = clientController.getClientOrders(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(orders, response.getBody());
        
        verify(authentication, times(1)).getName();
        verify(orderService, times(1)).getOrdersByClientEmail(email);
    }

    @Test
    public void testGetClientOrders_ServiceException() throws Exception {
        String email = "john@client.com";
        when(authentication.getName()).thenReturn(email);
        when(orderService.getOrdersByClientEmail(email)).thenThrow(new Exception("Database error"));

        ResponseEntity<?> response = clientController.getClientOrders(authentication);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error retrieving client orders", response.getBody());
        
        verify(authentication, times(1)).getName();
        verify(orderService, times(1)).getOrdersByClientEmail(email);
    }

    @Test
    public void testGetClientPoints_Success() throws Exception {
        String email = "john@client.com";
        Double currentBalance = 150.0;
        when(authentication.getName()).thenReturn(email);
        when(userService.getClientProfile(email)).thenReturn(sampleClientProfile);
        when(pointsService.getCurrentBalance(sampleClientProfile.getClientId())).thenReturn(currentBalance);

        ResponseEntity<?> response = clientController.getClientPoints(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Double> result = (Map<String, Double>) response.getBody();
        assertEquals(currentBalance, result.get("currentBalance"));
        
        verify(authentication, times(1)).getName();
        verify(userService, times(1)).getClientProfile(email);
        verify(pointsService, times(1)).getCurrentBalance(sampleClientProfile.getClientId());
    }

    @Test
    public void testGetClientPoints_ServiceException() throws Exception {
        String email = "john@client.com";
        when(authentication.getName()).thenReturn(email);
        when(userService.getClientProfile(email)).thenThrow(new Exception("User not found"));

        ResponseEntity<?> response = clientController.getClientPoints(authentication);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error retrieving client points", response.getBody());
        
        verify(authentication, times(1)).getName();
        verify(userService, times(1)).getClientProfile(email);
        verify(pointsService, never()).getCurrentBalance(anyString());
    }

    @Test
    public void testGetPointsHistory_Success() throws Exception {
        String email = "john@client.com";
        List<PointsTransaction> history = Arrays.asList(sampleTransaction);
        when(authentication.getName()).thenReturn(email);
        when(userService.getClientProfile(email)).thenReturn(sampleClientProfile);
        when(pointsService.getTransactionHistory(sampleClientProfile.getClientId())).thenReturn(history);

        ResponseEntity<?> response = clientController.getPointsHistory(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(history, response.getBody());
        
        verify(authentication, times(1)).getName();
        verify(userService, times(1)).getClientProfile(email);
        verify(pointsService, times(1)).getTransactionHistory(sampleClientProfile.getClientId());
    }

    @Test
    public void testGetPointsHistory_ServiceException() throws Exception {
        String email = "john@client.com";
        when(authentication.getName()).thenReturn(email);
        when(userService.getClientProfile(email)).thenReturn(sampleClientProfile);
        when(pointsService.getTransactionHistory(sampleClientProfile.getClientId()))
            .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = clientController.getPointsHistory(authentication);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error retrieving points history", response.getBody());
        
        verify(authentication, times(1)).getName();
        verify(userService, times(1)).getClientProfile(email);
        verify(pointsService, times(1)).getTransactionHistory(sampleClientProfile.getClientId());
    }

    @Test
    public void testGetClientOrders_EmptyList() throws Exception {
        String email = "john@client.com";
        List<Order> emptyOrders = Arrays.asList();
        when(authentication.getName()).thenReturn(email);
        when(orderService.getOrdersByClientEmail(email)).thenReturn(emptyOrders);

        ResponseEntity<?> response = clientController.getClientOrders(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptyOrders, response.getBody());
        
        verify(authentication, times(1)).getName();
        verify(orderService, times(1)).getOrdersByClientEmail(email);
    }

    @Test
    public void testGetPointsHistory_EmptyHistory() throws Exception {
        String email = "john@client.com";
        List<PointsTransaction> emptyHistory = Arrays.asList();
        when(authentication.getName()).thenReturn(email);
        when(userService.getClientProfile(email)).thenReturn(sampleClientProfile);
        when(pointsService.getTransactionHistory(sampleClientProfile.getClientId())).thenReturn(emptyHistory);

        ResponseEntity<?> response = clientController.getPointsHistory(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emptyHistory, response.getBody());
        
        verify(authentication, times(1)).getName();
        verify(userService, times(1)).getClientProfile(email);
        verify(pointsService, times(1)).getTransactionHistory(sampleClientProfile.getClientId());
    }


}
