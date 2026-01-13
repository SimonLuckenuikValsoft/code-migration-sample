package aim.legacy.services;

import java.util.List;

/**
 * ValidationException - thrown when order validation fails.
 */
public class ValidationException extends Exception {
    
    private final List<String> errors;
    
    public ValidationException(List<String> errors) {
        super(String.join(", ", errors));
        this.errors = errors;
    }
    
    public List<String> getErrors() {
        return errors;
    }
}
