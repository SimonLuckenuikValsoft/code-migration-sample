package aim.legacy.persistence;

import aim.legacy.domain.Product;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductRepository - handles persistence of products to JSON.
 * Legacy pattern: Simple repository with manual ID generation.
 */
public class ProductRepository {
    
    private final File dataFile;
    private List<Product> products;
    
    public ProductRepository(String dataPath) {
        this.dataFile = new File(dataPath, "products.json");
        this.products = new ArrayList<>();
        load();
    }
    
    /**
     * Load products from JSON file.
     */
    public void load() {
        try {
            if (dataFile.exists()) {
                products = JsonFileHelper.readList(dataFile, Product.class);
            } else {
                products = new ArrayList<>();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load products", e);
        }
    }
    
    /**
     * Save products to JSON file.
     */
    public void save() {
        try {
            JsonFileHelper.writeList(dataFile, products);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save products", e);
        }
    }
    
    /**
     * Get all products.
     */
    public List<Product> findAll() {
        return new ArrayList<>(products);
    }
    
    /**
     * Find product by ID.
     */
    public Product findById(Long id) {
        for (Product product : products) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        return null;
    }
    
    /**
     * Add or update product.
     */
    public void saveProduct(Product product) {
        if (product.getId() == null) {
            // New product - generate ID
            Long maxId = 0L;
            for (Product p : products) {
                if (p.getId() > maxId) {
                    maxId = p.getId();
                }
            }
            product.setId(maxId + 1);
            products.add(product);
        } else {
            // Update existing
            for (int i = 0; i < products.size(); i++) {
                if (products.get(i).getId().equals(product.getId())) {
                    products.set(i, product);
                    return;
                }
            }
            products.add(product);
        }
        save();
    }
}
