package aim.legacy.services;

import aim.legacy.domain.Order;
import aim.legacy.domain.OrderLine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderValidator - validates orders before saving.
 * Legacy pattern: Static validation methods that return error messages.
 * 
 * Validation Rules:
 * - Customer is required
 * - Must have at least 1 order line
 * - Quantity must be positive integer
 * - Unit price must be >= 0
 * - Discount cannot exceed 15%
 */
public class OrderValidator {
    
    public static final BigDecimal MAX_DISCOUNT_RATE = new BigDecimal("0.15");
    
    /**
     * Validate an order and return list of error messages.
     * Empty list means validation passed.
     */
    public static List<String> validate(Order order) {
        List<String> errors = new ArrayList<>();
        
        // Customer is required
        if (order.getCustomerId() == null) {
            errors.add("Customer is required");
        }
        
        // Must have at least one line
        if (order.getLines() == null || order.getLines().isEmpty()) {
            errors.add("Order must have at least one line item");
        } else {
            // Validate each line
            for (int i = 0; i < order.getLines().size(); i++) {
                OrderLine line = order.getLines().get(i);
                validateLine(line, i + 1, errors);
            }
        }
        
        // Validate discount percentage
        if (order.getSubtotal() != null && order.getDiscount() != null) {
            BigDecimal subtotal = order.getSubtotal();
            if (subtotal.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountRate = order.getDiscount().divide(subtotal, 4, BigDecimal.ROUND_HALF_UP);
                if (discountRate.compareTo(MAX_DISCOUNT_RATE) > 0) {
                    errors.add("Discount cannot exceed 15%");
                }
            }
        }
        
        return errors;
    }
    
    /**
     * Validate a single order line.
     */
    private static void validateLine(OrderLine line, int lineNumber, List<String> errors) {
        // Quantity must be positive
        if (line.getQuantity() <= 0) {
            errors.add("Line " + lineNumber + ": Quantity must be positive");
        }
        
        // Unit price must be >= 0
        if (line.getUnitPrice() == null || line.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Line " + lineNumber + ": Unit price must be zero or greater");
        }
        
        // Product must be set
        if (line.getProductId() == null) {
            errors.add("Line " + lineNumber + ": Product is required");
        }
    }
    
    /**
     * Check if order is valid.
     */
    public static boolean isValid(Order order) {
        return validate(order).isEmpty();
    }
}
