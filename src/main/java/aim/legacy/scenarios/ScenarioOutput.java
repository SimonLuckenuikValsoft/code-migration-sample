package aim.legacy.scenarios;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * ScenarioOutput - represents output of a scenario run.
 * Uses TreeMap for deterministic ordering of fields.
 */
public class ScenarioOutput {
    private String scenarioName;
    private Map<String, Object> result;
    private List<String> validationErrors;

    public ScenarioOutput() {
        this.result = new TreeMap<>();
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
