package aim.legacy.scenarios;

import aim.legacy.domain.OrderLine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ScenarioInput - represents input for a pricing/validation scenario.
 */
public class ScenarioInput {
    private String scenarioName;
    private Long customerId;
    private List<ScenarioLine> lines;

    public ScenarioInput() {
        this.lines = new ArrayList<>();
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<ScenarioLine> getLines() {
        return lines;
    }

    public void setLines(List<ScenarioLine> lines) {
        this.lines = lines;
    }

    public static class ScenarioLine {
        private Long productId;
        private int quantity;
        private BigDecimal unitPrice;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }
    }
}
