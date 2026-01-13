package aim.legacy.scenarios;

import aim.legacy.domain.Order;
import aim.legacy.domain.OrderLine;
import aim.legacy.services.OrderValidator;
import aim.legacy.services.PricingCalculator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * ScenarioRunner - runs pricing and validation scenarios from JSON files.
 * Main class: aim.legacy.scenarios.ScenarioRunner
 * 
 * Usage: java -cp ... aim.legacy.scenarios.ScenarioRunner <scenario-file-path>
 * 
 * Output is canonical JSON to stdout for parity testing.
 */
public class ScenarioRunner {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("0.00");

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

    /**
     * Run scenarios from file and output results to stdout.
     */
    public static void runScenarios(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("Scenario file not found: " + filePath);
        }

        // Read scenarios
        ObjectMapper mapper = new ObjectMapper();
        ScenarioInput[] scenarios = mapper.readValue(file, ScenarioInput[].class);

        // Process each scenario
        List<ScenarioOutput> outputs = new ArrayList<>();
        for (ScenarioInput input : scenarios) {
            ScenarioOutput output = processScenario(input);
            outputs.add(output);
        }

        // Output results as canonical JSON
        ObjectMapper outputMapper = createCanonicalMapper();
        String json = outputMapper.writeValueAsString(outputs);
        System.out.println(json);
    }

    /**
     * Process a single scenario.
     */
    static ScenarioOutput processScenario(ScenarioInput input) {
        ScenarioOutput output = new ScenarioOutput();
        output.setScenarioName(input.getScenarioName());

        // Build order
        Order order = new Order();
        order.setCustomerId(input.getCustomerId());
        order.setCustomerName("Test Customer");

        for (ScenarioInput.ScenarioLine scenarioLine : input.getLines()) {
            OrderLine line = new OrderLine();
            line.setProductId(scenarioLine.getProductId());
            line.setProductName("Product " + scenarioLine.getProductId());
            line.setQuantity(scenarioLine.getQuantity());
            line.setUnitPrice(scenarioLine.getUnitPrice());
            order.addLine(line);
        }

        // Calculate pricing
        PricingCalculator.calculateOrderPricing(order);

        // Validate
        List<String> errors = OrderValidator.validate(order);

        // Build result with deterministic ordering
        TreeMap<String, Object> result = new TreeMap<>();
        result.put("discount", formatMoney(order.getDiscount()));
        result.put("subtotal", formatMoney(order.getSubtotal()));
        result.put("tax", formatMoney(order.getTax()));
        result.put("total", formatMoney(order.getTotal()));

        output.setResult(result);
        if (!errors.isEmpty()) {
            output.setValidationErrors(errors);
        }

        return output;
    }

    /**
     * Create ObjectMapper configured for canonical output.
     */
    private static ObjectMapper createCanonicalMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        return mapper;
    }

    /**
     * Format BigDecimal as string with 2 decimals.
     */
    private static String formatMoney(BigDecimal value) {
        return MONEY_FORMAT.format(value);
    }
}
