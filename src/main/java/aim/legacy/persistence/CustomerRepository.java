package aim.legacy.persistence;

import aim.legacy.domain.Customer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CustomerRepository - handles persistence of customers to JSON.
 * Legacy pattern: Simple repository with manual ID generation.
 */
public class CustomerRepository {
    
    private final File dataFile;
    private List<Customer> customers;
    
    public CustomerRepository(String dataPath) {
        this.dataFile = new File(dataPath, "customers.json");
        this.customers = new ArrayList<>();
        load();
    }
    
    /**
     * Load customers from JSON file.
     */
    public void load() {
        try {
            if (dataFile.exists()) {
                customers = JsonFileHelper.readList(dataFile, Customer.class);
            } else {
                customers = new ArrayList<>();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load customers", e);
        }
    }
    
    /**
     * Save customers to JSON file.
     */
    public void save() {
        try {
            JsonFileHelper.writeList(dataFile, customers);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save customers", e);
        }
    }
    
    /**
     * Get all customers.
     */
    public List<Customer> findAll() {
        return new ArrayList<>(customers);
    }
    
    /**
     * Find customer by ID.
     */
    public Customer findById(Long id) {
        for (Customer customer : customers) {
            if (customer.getId().equals(id)) {
                return customer;
            }
        }
        return null;
    }
    
    /**
     * Search customers by name.
     */
    public List<Customer> searchByName(String query) {
        List<Customer> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Customer customer : customers) {
            if (customer.getName().toLowerCase().contains(lowerQuery)) {
                results.add(customer);
            }
        }
        return results;
    }
    
    /**
     * Add or update customer.
     * Legacy pattern: Manual ID generation.
     */
    public void saveCustomer(Customer customer) {
        if (customer.getId() == null) {
            // New customer - generate ID
            Long maxId = 0L;
            for (Customer c : customers) {
                if (c.getId() > maxId) {
                    maxId = c.getId();
                }
            }
            customer.setId(maxId + 1);
            customers.add(customer);
        } else {
            // Update existing
            for (int i = 0; i < customers.size(); i++) {
                if (customers.get(i).getId().equals(customer.getId())) {
                    customers.set(i, customer);
                    return;
                }
            }
            // Not found, add as new
            customers.add(customer);
        }
        save();
    }
    
    /**
     * Delete customer.
     */
    public void deleteCustomer(Long id) {
        customers.removeIf(c -> c.getId().equals(id));
        save();
    }
}
