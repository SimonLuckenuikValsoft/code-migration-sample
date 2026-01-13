package aim.legacy.persistence;

import aim.legacy.domain.Order;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderRepository - handles persistence of orders to JSON.
 * Legacy pattern: Simple repository with manual ID generation.
 */
public class OrderRepository {
    
    private final File dataFile;
    private List<Order> orders;
    
    public OrderRepository(String dataPath) {
        this.dataFile = new File(dataPath, "orders.json");
        this.orders = new ArrayList<>();
        load();
    }
    
    /**
     * Load orders from JSON file.
     */
    public void load() {
        try {
            if (dataFile.exists()) {
                orders = JsonFileHelper.readList(dataFile, Order.class);
            } else {
                orders = new ArrayList<>();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load orders", e);
        }
    }
    
    /**
     * Save orders to JSON file.
     */
    public void save() {
        try {
            JsonFileHelper.writeList(dataFile, orders);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save orders", e);
        }
    }
    
    /**
     * Get all orders.
     */
    public List<Order> findAll() {
        return new ArrayList<>(orders);
    }
    
    /**
     * Find order by ID.
     */
    public Order findById(Long id) {
        for (Order order : orders) {
            if (order.getId().equals(id)) {
                return order;
            }
        }
        return null;
    }
    
    /**
     * Add or update order.
     */
    public void saveOrder(Order order) {
        if (order.getId() == null) {
            // New order - generate ID
            Long maxId = 0L;
            for (Order o : orders) {
                if (o.getId() > maxId) {
                    maxId = o.getId();
                }
            }
            order.setId(maxId + 1);
            
            // Also generate IDs for lines
            long lineId = 1;
            for (int i = 0; i < order.getLines().size(); i++) {
                if (order.getLines().get(i).getId() == null) {
                    order.getLines().get(i).setId(lineId++);
                }
            }
            
            orders.add(order);
        } else {
            // Update existing
            for (int i = 0; i < orders.size(); i++) {
                if (orders.get(i).getId().equals(order.getId())) {
                    orders.set(i, order);
                    return;
                }
            }
            orders.add(order);
        }
        save();
    }
    
    /**
     * Delete order.
     */
    public void deleteOrder(Long id) {
        orders.removeIf(o -> o.getId().equals(id));
        save();
    }
}
