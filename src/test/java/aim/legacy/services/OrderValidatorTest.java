package aim.legacy.services;

import aim.legacy.domain.Order;
import aim.legacy.domain.OrderLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for OrderValidator.
 * Tests validation rules for orders and order lines.
 */
class OrderValidatorTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setCustomerId(1L);
        order.setCustomerName("Test Customer");
    }

    @Test
    void testValidOrderPasses() {
        addLine(order, 1L, "Product A", 1, "100.00");
        PricingCalculator.calculateOrderPricing(order);
        
        List<String> errors = OrderValidator.validate(order);
        assertTrue(errors.isEmpty());
        assertTrue(OrderValidator.isValid(order));
    }

    @Test
    void testMissingCustomerFails() {
        order.setCustomerId(null);
        addLine(order, 1L, "Product A", 1, "100.00");
        
        List<String> errors = OrderValidator.validate(order);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Customer is required")));
    }

    @Test
    void testEmptyLinesFails() {
        order.getLines().clear();
        
        List<String> errors = OrderValidator.validate(order);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("at least one line")));
    }

    @Test
    void testNullLinesFails() {
        order.setLines(null);
        
        List<String> errors = OrderValidator.validate(order);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("at least one line")));
    }

    @Test
    void testNegativeQuantityFails() {
        addLine(order, 1L, "Product A", -1, "100.00");
        
        List<String> errors = OrderValidator.validate(order);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Quantity must be positive")));
    }

    @Test
    void testZeroQuantityFails() {
        addLine(order, 1L, "Product A", 0, "100.00");
        
        List<String> errors = OrderValidator.validate(order);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Quantity must be positive")));
    }

    @Test
    void testNegativeUnitPriceFails() {
        addLine(order, 1L, "Product A", 1, "-10.00");
        
        List<String> errors = OrderValidator.validate(order);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Unit price must be zero or greater")));
    }

    @Test
    void testZeroUnitPricePasses() {
        addLine(order, 1L, "Product A", 1, "0.00");
        PricingCalculator.calculateOrderPricing(order);
        
        List<String> errors = OrderValidator.validate(order);
        assertTrue(errors.isEmpty());
    }

    @Test
    void testNullUnitPriceFails() {
        OrderLine line = new OrderLine();
        line.setProductId(1L);
        line.setProductName("Product A");
        line.setQuantity(1);
        line.setUnitPrice(null);
        order.addLine(line);
        
        List<String> errors = OrderValidator.validate(order);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Unit price must be zero or greater")));
    }

    @Test
    void testMissingProductIdFails() {
        OrderLine line = new OrderLine();
        line.setProductId(null);
        line.setProductName("Product A");
        line.setQuantity(1);
        line.setUnitPrice(new BigDecimal("100.00"));
        order.addLine(line);
        
        List<String> errors = OrderValidator.validate(order);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Product is required")));
    }

    @Test
    void testDiscountExceedsFifteenPercentFails() {
        addLine(order, 1L, "Product A", 1, "1000.00");
        order.setSubtotal(new BigDecimal("1000.00"));
        order.setDiscount(new BigDecimal("160.00")); // 16%
        
        List<String> errors = OrderValidator.validate(order);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Discount cannot exceed 15%")));
    }

    @Test
    void testDiscountAtFifteenPercentPasses() {
        addLine(order, 1L, "Product A", 1, "1000.00");
        order.setSubtotal(new BigDecimal("1000.00"));
        order.setDiscount(new BigDecimal("150.00")); // 15%
        
        List<String> errors = OrderValidator.validate(order);
        // Should not have discount error
        assertFalse(errors.stream().anyMatch(e -> e.contains("Discount cannot exceed 15%")));
    }

    @Test
    void testMultipleValidationErrors() {
        order.setCustomerId(null);
        addLine(order, null, "Product A", -1, "-10.00");
        
        List<String> errors = OrderValidator.validate(order);
        assertTrue(errors.size() >= 4); // Customer, product, quantity, price
    }

    @Test
    void testLineNumberInErrorMessage() {
        addLine(order, 1L, "Product A", 1, "100.00");
        addLine(order, 1L, "Product B", -1, "50.00");
        
        List<String> errors = OrderValidator.validate(order);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("Line 2")));
    }

    // Helper method
    private void addLine(Order order, Long productId, String productName, int quantity, String unitPrice) {
        OrderLine line = new OrderLine();
        line.setProductId(productId);
        line.setProductName(productName);
        line.setQuantity(quantity);
        line.setUnitPrice(new BigDecimal(unitPrice));
        order.addLine(line);
    }
}
