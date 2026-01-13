package aim.legacy.services;

import aim.legacy.domain.Order;
import aim.legacy.domain.OrderLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for PricingCalculator.
 * Tests cover discount thresholds, tax calculation, rounding behavior, and edge cases.
 */
class PricingCalculatorTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setCustomerId(1L);
        order.setCustomerName("Test Customer");
    }

    // Discount threshold tests

    @Test
    void testNoDiscountBelowThreshold() {
        addLine(order, 1L, "Product A", 1, "499.99");
        PricingCalculator.calculateOrderPricing(order);
        
        assertEquals(new BigDecimal("499.99"), order.getSubtotal());
        assertEquals(new BigDecimal("0.00"), order.getDiscount());
    }

    @Test
    void testFivePercentDiscountAtThreshold() {
        addLine(order, 1L, "Product A", 1, "500.00");
        PricingCalculator.calculateOrderPricing(order);
        
        assertEquals(new BigDecimal("500.00"), order.getSubtotal());
        assertEquals(new BigDecimal("25.00"), order.getDiscount());
    }

    @Test
    void testFivePercentDiscountAboveThreshold() {
        addLine(order, 1L, "Product A", 1, "750.00");
        PricingCalculator.calculateOrderPricing(order);
        
        assertEquals(new BigDecimal("750.00"), order.getSubtotal());
        assertEquals(new BigDecimal("37.50"), order.getDiscount());
    }

    @Test
    void testTenPercentDiscountAtThreshold() {
        addLine(order, 1L, "Product A", 1, "1000.00");
        PricingCalculator.calculateOrderPricing(order);
        
        assertEquals(new BigDecimal("1000.00"), order.getSubtotal());
        assertEquals(new BigDecimal("100.00"), order.getDiscount());
    }

    @Test
    void testTenPercentDiscountAboveThreshold() {
        addLine(order, 1L, "Product A", 1, "1500.00");
        PricingCalculator.calculateOrderPricing(order);
        
        assertEquals(new BigDecimal("1500.00"), order.getSubtotal());
        assertEquals(new BigDecimal("150.00"), order.getDiscount());
    }

    @Test
    void testFifteenPercentDiscountAtThreshold() {
        addLine(order, 1L, "Product A", 1, "2000.00");
        PricingCalculator.calculateOrderPricing(order);
        
        assertEquals(new BigDecimal("2000.00"), order.getSubtotal());
        assertEquals(new BigDecimal("300.00"), order.getDiscount());
    }

    @Test
    void testFifteenPercentDiscountAboveThreshold() {
        addLine(order, 1L, "Product A", 1, "3000.00");
        PricingCalculator.calculateOrderPricing(order);
        
        assertEquals(new BigDecimal("3000.00"), order.getSubtotal());
        assertEquals(new BigDecimal("450.00"), order.getDiscount());
    }

    // Tax calculation tests

    @Test
    void testTaxCalculationAfterDiscount() {
        addLine(order, 1L, "Product A", 1, "1000.00");
        PricingCalculator.calculateOrderPricing(order);
        
        // Tax on (1000 - 100) = 900 * 0.14975 = 134.775 -> rounds to 134.78
        assertEquals(new BigDecimal("134.78"), order.getTax());
    }

    @Test
    void testTaxCalculationWithoutDiscount() {
        addLine(order, 1L, "Product A", 1, "100.00");
        PricingCalculator.calculateOrderPricing(order);
        
        // Tax on 100 * 0.14975 = 14.975 -> rounds to 14.98
        assertEquals(new BigDecimal("14.98"), order.getTax());
    }

    // Rounding tests

    @Test
    void testRoundingHalfUpAtHalfCent() {
        addLine(order, 1L, "Product A", 1, "10.005");
        PricingCalculator.calculateOrderPricing(order);
        
        // Should round 10.005 up to 10.01
        assertEquals(new BigDecimal("10.01"), order.getSubtotal());
    }

    @Test
    void testRoundingHalfUpBelowHalf() {
        addLine(order, 1L, "Product A", 1, "10.004");
        PricingCalculator.calculateOrderPricing(order);
        
        // Should round 10.004 down to 10.00
        assertEquals(new BigDecimal("10.00"), order.getSubtotal());
    }

    @Test
    void testRoundingHalfUpAboveHalf() {
        addLine(order, 1L, "Product A", 1, "10.006");
        PricingCalculator.calculateOrderPricing(order);
        
        // Should round 10.006 up to 10.01
        assertEquals(new BigDecimal("10.01"), order.getSubtotal());
    }

    @Test
    void testTaxRoundingHalfUp() {
        // Create order that will result in tax needing rounding
        addLine(order, 1L, "Product A", 1, "66.67");
        PricingCalculator.calculateOrderPricing(order);
        
        // Tax = 66.67 * 0.14975 = 9.9848325 -> rounds to 9.98
        assertEquals(new BigDecimal("9.98"), order.getTax());
    }

    // Total calculation tests

    @Test
    void testTotalCalculation() {
        addLine(order, 1L, "Product A", 1, "1000.00");
        PricingCalculator.calculateOrderPricing(order);
        
        // Subtotal: 1000.00
        // Discount: 100.00
        // Tax: 134.78
        // Total: 1000 - 100 + 134.78 = 1034.78
        assertEquals(new BigDecimal("1034.78"), order.getTotal());
    }

    @Test
    void testTotalWithMultipleLines() {
        addLine(order, 1L, "Product A", 2, "250.00");
        addLine(order, 2L, "Product B", 1, "500.00");
        PricingCalculator.calculateOrderPricing(order);
        
        // Subtotal: 1000.00
        // Discount: 100.00 (10%)
        // Tax: 134.78
        // Total: 1034.78
        assertEquals(new BigDecimal("1000.00"), order.getSubtotal());
        assertEquals(new BigDecimal("100.00"), order.getDiscount());
        assertEquals(new BigDecimal("134.78"), order.getTax());
        assertEquals(new BigDecimal("1034.78"), order.getTotal());
    }

    // Edge case tests

    @Test
    void testZeroSubtotal() {
        // Empty order
        PricingCalculator.calculateOrderPricing(order);
        
        assertEquals(new BigDecimal("0.00"), order.getSubtotal());
        assertEquals(new BigDecimal("0.00"), order.getDiscount());
        assertEquals(new BigDecimal("0.00"), order.getTax());
        assertEquals(new BigDecimal("0.00"), order.getTotal());
    }

    @Test
    void testJustBelowThreshold() {
        addLine(order, 1L, "Product A", 1, "499.99");
        PricingCalculator.calculateOrderPricing(order);
        
        assertEquals(new BigDecimal("0.00"), order.getDiscount());
    }

    @Test
    void testJustBelowSecondThreshold() {
        addLine(order, 1L, "Product A", 1, "999.99");
        PricingCalculator.calculateOrderPricing(order);
        
        // Should get 5% discount, not 10%
        assertEquals(new BigDecimal("50.00"), order.getDiscount());
    }

    @Test
    void testJustBelowThirdThreshold() {
        addLine(order, 1L, "Product A", 1, "1999.99");
        PricingCalculator.calculateOrderPricing(order);
        
        // Should get 10% discount, not 15%
        assertEquals(new BigDecimal("200.00"), order.getDiscount());
    }

    @Test
    void testComplexOrderWithManyLines() {
        addLine(order, 1L, "Laptop", 2, "1299.99");
        addLine(order, 2L, "Mouse", 2, "29.99");
        addLine(order, 3L, "Keyboard", 1, "149.99");
        PricingCalculator.calculateOrderPricing(order);
        
        // Subtotal: 2599.98 + 59.98 + 149.99 = 2809.95
        // Discount: 421.49 (15%)
        // Taxable: 2388.46
        // Tax: 357.67
        // Total: 2746.13
        assertEquals(new BigDecimal("2809.95"), order.getSubtotal());
        assertEquals(new BigDecimal("421.49"), order.getDiscount());
        assertEquals(new BigDecimal("357.67"), order.getTax());
        assertEquals(new BigDecimal("2746.13"), order.getTotal());
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
