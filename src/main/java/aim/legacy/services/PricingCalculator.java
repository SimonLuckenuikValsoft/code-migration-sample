package aim.legacy.services;

import aim.legacy.domain.Order;
import aim.legacy.domain.OrderLine;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * PricingCalculator - handles all pricing calculations for orders.
 * Legacy pattern: Static utility methods for calculations.
 * 
 * Business Rules:
 * - Subtotal is sum of line totals (qty * unitPrice)
 * - Discount tiers based on subtotal:
 *   >= 500: 5%
 *   >= 1000: 10%
 *   >= 2000: 15%
 * - Tax rate: 14.975% (applied after discount)
 * - All monetary values rounded to 2 decimals using HALF_UP
 */
public class PricingCalculator {
    
    // Tax rate constant
    public static final BigDecimal TAX_RATE = new BigDecimal("0.14975");
    
    // Discount thresholds and percentages
    public static final BigDecimal DISCOUNT_THRESHOLD_1 = new BigDecimal("500.00");
    public static final BigDecimal DISCOUNT_THRESHOLD_2 = new BigDecimal("1000.00");
    public static final BigDecimal DISCOUNT_THRESHOLD_3 = new BigDecimal("2000.00");
    
    public static final BigDecimal DISCOUNT_RATE_1 = new BigDecimal("0.05");
    public static final BigDecimal DISCOUNT_RATE_2 = new BigDecimal("0.10");
    public static final BigDecimal DISCOUNT_RATE_3 = new BigDecimal("0.15");
    
    /**
     * Calculate subtotal from order lines.
     * Subtotal is the sum of all line totals before any discounts or tax.
     */
    public static BigDecimal calculateSubtotal(Order order) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderLine line : order.getLines()) {
            subtotal = subtotal.add(line.getLineTotal());
        }
        return round(subtotal);
    }
    
    /**
     * Calculate discount amount based on subtotal.
     * Uses tiered discount structure.
     */
    public static BigDecimal calculateDiscount(BigDecimal subtotal) {
        BigDecimal discountRate = getDiscountRate(subtotal);
        BigDecimal discount = subtotal.multiply(discountRate);
        return round(discount);
    }
    
    /**
     * Get discount rate based on subtotal amount.
     * Legacy pattern: Simple if-else chain.
     */
    public static BigDecimal getDiscountRate(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD_3) >= 0) {
            return DISCOUNT_RATE_3;
        } else if (subtotal.compareTo(DISCOUNT_THRESHOLD_2) >= 0) {
            return DISCOUNT_RATE_2;
        } else if (subtotal.compareTo(DISCOUNT_THRESHOLD_1) >= 0) {
            return DISCOUNT_RATE_1;
        } else {
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Calculate tax on the amount after discount.
     * Tax = (subtotal - discount) * tax rate
     */
    public static BigDecimal calculateTax(BigDecimal subtotal, BigDecimal discount) {
        BigDecimal taxableAmount = subtotal.subtract(discount);
        BigDecimal tax = taxableAmount.multiply(TAX_RATE);
        return round(tax);
    }
    
    /**
     * Calculate total: subtotal - discount + tax
     */
    public static BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal discount, BigDecimal tax) {
        BigDecimal total = subtotal.subtract(discount).add(tax);
        return round(total);
    }
    
    /**
     * Calculate all pricing fields for an order.
     * Legacy pattern: "God method" that updates the order in place.
     */
    public static void calculateOrderPricing(Order order) {
        BigDecimal subtotal = calculateSubtotal(order);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal tax = calculateTax(subtotal, discount);
        BigDecimal total = calculateTotal(subtotal, discount, tax);
        
        // Update order fields in place (legacy pattern)
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setTax(tax);
        order.setTotal(total);
    }
    
    /**
     * Round monetary value to 2 decimal places using HALF_UP.
     */
    public static BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
