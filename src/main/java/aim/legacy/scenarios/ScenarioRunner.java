package aim.legacy.scenarios;

import aim.legacy.domain.Order;
import aim.legacy.domain.OrderLine;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * ScenarioRunner - runs pricing and validation scenarios from JSON files.
 */
public class ScenarioRunner {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("0.00");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.14975");

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: ScenarioRunner <scenario-file-path>");
            System.exit(1);
        }

        String scenarioFilePath = args[0];
        
        try {
            runScenarios(scenarioFilePath);
        } catch (Exception e) {
            System.err.println("Error running scenarios: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void runScenarios(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("Scenario file not found: " + filePath);
        }

        ObjectMapper mapper = new ObjectMapper();
        
        List<Map<String, Object>> scenarios = mapper.readValue(file, List.class);

        List<Map<String, Object>> outputs = new ArrayList<>();
        for (Map<String, Object> input : scenarios) {
            Map<String, Object> output = processScenario(input);
            outputs.add(output);
        }

        ObjectMapper outputMapper = new ObjectMapper();
        outputMapper.enable(SerializationFeature.INDENT_OUTPUT);
        outputMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        String json = outputMapper.writeValueAsString(outputs);
        System.out.println(json);
    }

    static Map<String, Object> processScenario(Map<String, Object> input) {
        String scenarioName = (String) input.get("scenarioName");
        Long customerId = input.get("customerId") != null ? 
            ((Number) input.get("customerId")).longValue() : null;
        List<Map<String, Object>> lines = (List<Map<String, Object>>) input.get("lines");

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setCustomerName("Test Customer");
        order.setLines(new ArrayList<>());

        if (lines != null) {
            for (Map<String, Object> lineData : lines) {
                OrderLine line = new OrderLine();
                line.setProductId(lineData.get("productId") != null ? 
                    ((Number) lineData.get("productId")).longValue() : null);
                line.setProductName("Product " + line.getProductId());
                line.setQuantity(((Number) lineData.get("quantity")).intValue());
                
                Object priceObj = lineData.get("unitPrice");
                BigDecimal unitPrice;
                if (priceObj instanceof Double) {
                    unitPrice = BigDecimal.valueOf((Double) priceObj);
                } else if (priceObj instanceof Integer) {
                    unitPrice = BigDecimal.valueOf((Integer) priceObj);
                } else {
                    unitPrice = new BigDecimal(priceObj.toString());
                }
                line.setUnitPrice(unitPrice);
                
                order.getLines().add(line);
            }
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderLine line : order.getLines()) {
            BigDecimal lineTotal = line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
            subtotal = subtotal.add(lineTotal);
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal discount = BigDecimal.ZERO;
        if (subtotal.compareTo(new BigDecimal("2000")) >= 0) {
            discount = subtotal.multiply(new BigDecimal("0.15"));
        } else if (subtotal.compareTo(new BigDecimal("1000")) >= 0) {
            discount = subtotal.multiply(new BigDecimal("0.10"));
        } else if (subtotal.compareTo(new BigDecimal("500")) >= 0) {
            discount = subtotal.multiply(new BigDecimal("0.05"));
        }
        discount = discount.setScale(2, RoundingMode.HALF_UP);
        
        BigDecimal taxableAmount = subtotal.subtract(discount);
        BigDecimal tax = taxableAmount.multiply(TAX_RATE);
        tax = tax.setScale(2, RoundingMode.HALF_UP);
        
        // Calculate total
        BigDecimal total = subtotal.subtract(discount).add(tax);
        total = total.setScale(2, RoundingMode.HALF_UP);
        
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setTax(tax);
        order.setTotal(total);

        List<String> errors = new ArrayList<>();
        
        if (customerId == null) {
            errors.add("Customer is required");
        }
        
        if (order.getLines() == null || order.getLines().isEmpty()) {
            errors.add("Order must have at least one line item");
        } else {
            for (int i = 0; i < order.getLines().size(); i++) {
                OrderLine line = order.getLines().get(i);
                if (line.getQuantity() <= 0) {
                    errors.add("Line " + (i + 1) + ": Quantity must be positive");
                }
                if (line.getUnitPrice() == null || line.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                    errors.add("Line " + (i + 1) + ": Unit price must be zero or greater");
                }
                if (line.getProductId() == null) {
                    errors.add("Line " + (i + 1) + ": Product is required");
                }
            }
        }
        
        if (order.getSubtotal() != null && order.getSubtotal().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountRate = order.getDiscount().divide(order.getSubtotal(), 4, RoundingMode.HALF_UP);
            if (discountRate.compareTo(new BigDecimal("0.15")) > 0) {
                errors.add("Discount cannot exceed 15%");
            }
        }

        TreeMap<String, Object> result = new TreeMap<>();
        result.put("discount", formatMoney(order.getDiscount()));
        result.put("subtotal", formatMoney(order.getSubtotal()));
        result.put("tax", formatMoney(order.getTax()));
        result.put("total", formatMoney(order.getTotal()));

        Map<String, Object> output = new TreeMap<>();
        output.put("scenarioName", scenarioName);
        output.put("result", result);
        if (!errors.isEmpty()) {
            output.put("validationErrors", errors);
        } else {
            output.put("validationErrors", null);
        }

        return output;
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) return "0.00";
        return MONEY_FORMAT.format(value);
    }
}
