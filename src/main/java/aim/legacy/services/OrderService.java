package aim.legacy.services;

import aim.legacy.domain.Customer;
import aim.legacy.domain.Order;
import aim.legacy.domain.OrderLine;
import aim.legacy.domain.Product;
import aim.legacy.persistence.CustomerRepository;
import aim.legacy.persistence.OrderRepository;
import aim.legacy.persistence.ProductRepository;

import java.util.List;

/**
 * OrderService - "God service" class that handles all order-related operations.
 * Legacy pattern: Centralized service with tight coupling to repositories.
 * This intentionally demonstrates legacy patterns that would be refactored in a modern application.
 */
public class OrderService {
    
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    
    // Singleton pattern (legacy)
    private static OrderService instance;
    
    private OrderService(String dataPath) {
        this.customerRepository = new CustomerRepository(dataPath);
        this.productRepository = new ProductRepository(dataPath);
        this.orderRepository = new OrderRepository(dataPath);
    }
    
    /**
     * Get singleton instance.
     * Legacy pattern: Singleton service.
     */
    public static OrderService getInstance(String dataPath) {
        if (instance == null) {
            instance = new OrderService(dataPath);
        }
        return instance;
    }
    
    /**
     * Reset singleton (for testing).
     */
    public static void resetInstance() {
        instance = null;
    }
    
    // Customer operations
    
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    public Customer getCustomer(Long id) {
        return customerRepository.findById(id);
    }
    
    public List<Customer> searchCustomers(String query) {
        return customerRepository.searchByName(query);
    }
    
    public void saveCustomer(Customer customer) {
        customerRepository.saveCustomer(customer);
    }
    
    public void deleteCustomer(Long id) {
        customerRepository.deleteCustomer(id);
    }
    
    // Product operations
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public Product getProduct(Long id) {
        return productRepository.findById(id);
    }
    
    // Order operations
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public Order getOrder(Long id) {
        return orderRepository.findById(id);
    }
    
    /**
     * Create new order for a customer.
     * Legacy pattern: Service layer creating domain objects.
     */
    public Order createOrder(Long customerId) {
        Customer customer = customerRepository.findById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        }
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setCustomerName(customer.getName());
        return order;
    }
    
    /**
     * Add line to order.
     * Legacy pattern: Duplicated logic - product lookup here and in UI.
     */
    public void addLineToOrder(Order order, Long productId, int quantity) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        
        OrderLine line = new OrderLine();
        line.setProductId(productId);
        line.setProductName(product.getName());
        line.setQuantity(quantity);
        line.setUnitPrice(product.getUnitPrice());
        
        order.addLine(line);
        
        // Recalculate pricing after adding line
        PricingCalculator.calculateOrderPricing(order);
    }
    
    /**
     * Save order with validation and pricing calculation.
     * Legacy pattern: Multiple responsibilities in one method.
     */
    public void saveOrder(Order order) throws ValidationException {
        // Calculate pricing
        PricingCalculator.calculateOrderPricing(order);
        
        // Validate
        List<String> errors = OrderValidator.validate(order);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        
        // Save
        orderRepository.saveOrder(order);
    }
    
    public void deleteOrder(Long id) {
        orderRepository.deleteOrder(id);
    }
    
    /**
     * Reload all data from files.
     */
    public void reloadData() {
        customerRepository.load();
        productRepository.load();
        orderRepository.load();
    }
}
