package aim.legacy.ui;

import aim.legacy.domain.Customer;
import aim.legacy.domain.Order;
import aim.legacy.domain.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * MainApp - main entry point for the desktop application.
 * 
 * TECHNICAL DEBT WARNING:
 * This class violates multiple design principles:
 * - Handles UI, business logic, AND data access all in one place
 * - Static data lists shared across the application
 * - Direct file I/O mixed with UI code
 * - No separation of concerns
 * - No error handling in many places
 * 
 * This is intentionally poor design to simulate legacy code.
 */
public class MainApp extends JFrame {

    private static final String DATA_PATH = "data";
    
    // TECHNICAL DEBT: Global static state - bad practice!
    public static List<Customer> allCustomers = new ArrayList<>();
    public static List<Product> allProducts = new ArrayList<>();
    public static List<Order> allOrders = new ArrayList<>();
    public static ObjectMapper mapper;
    
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    private CustomersScreen customersScreen;
    private OrdersScreen ordersScreen;
    
    // TECHNICAL DEBT: Static initializer doing I/O - dangerous!
    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        
        // Load data on startup - no error handling!
        loadAllData();
    }
    
    public MainApp() {
        super("Order Entry System - Legacy Edition");
        
        // Setup UI
        setupUI();
        
        // Window settings
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
    }
    
    private void setupUI() {
        // Create menu bar
        setJMenuBar(createMenuBar());
        
        // Create main panel with card layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Create screens
        customersScreen = new CustomersScreen(this);
        ordersScreen = new OrdersScreen(this);
        
        // Add screens to card layout
        mainPanel.add(customersScreen, "customers");
        mainPanel.add(ordersScreen, "orders");
        
        // Add to frame
        add(mainPanel);
        
        // Show customers screen by default
        showCustomersScreen();
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu menu = new JMenu("Navigation");
        
        JMenuItem customersItem = new JMenuItem("Customers");
        customersItem.addActionListener(e -> showCustomersScreen());
        menu.add(customersItem);
        
        JMenuItem ordersItem = new JMenuItem("Orders");
        ordersItem.addActionListener(e -> showOrdersScreen());
        menu.add(ordersItem);
        
        menu.addSeparator();
        
        JMenuItem reloadItem = new JMenuItem("Reload Data");
        reloadItem.addActionListener(e -> {
            loadAllData();
            customersScreen.refresh();
            ordersScreen.refresh();
            JOptionPane.showMessageDialog(this, "Data reloaded successfully");
        });
        menu.add(reloadItem);
        
        menu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        menu.add(exitItem);
        
        menuBar.add(menu);
        
        return menuBar;
    }
    
    public void showCustomersScreen() {
        customersScreen.refresh();
        cardLayout.show(mainPanel, "customers");
    }
    
    public void showOrdersScreen() {
        ordersScreen.refresh();
        cardLayout.show(mainPanel, "orders");
    }
    
    // TECHNICAL DEBT: Static method doing file I/O - tightly coupled to file system
    public static void loadAllData() {
        try {
            File customersFile = new File(DATA_PATH, "customers.json");
            if (customersFile.exists()) {
                allCustomers = mapper.readValue(customersFile, 
                    mapper.getTypeFactory().constructCollectionType(List.class, Customer.class));
            }
            
            File productsFile = new File(DATA_PATH, "products.json");
            if (productsFile.exists()) {
                allProducts = mapper.readValue(productsFile, 
                    mapper.getTypeFactory().constructCollectionType(List.class, Product.class));
            }
            
            File ordersFile = new File(DATA_PATH, "orders.json");
            if (ordersFile.exists()) {
                allOrders = mapper.readValue(ordersFile, 
                    mapper.getTypeFactory().constructCollectionType(List.class, Order.class));
            }
        } catch (IOException e) {
            // TECHNICAL DEBT: Swallowing exceptions - bad practice!
            e.printStackTrace();
        }
    }
    
    // TECHNICAL DEBT: More static methods mixing I/O with application logic
    public static void saveCustomers() {
        try {
            File file = new File(DATA_PATH, "customers.json");
            mapper.writeValue(file, allCustomers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void saveOrders() {
        try {
            File file = new File(DATA_PATH, "orders.json");
            mapper.writeValue(file, allOrders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // TECHNICAL DEBT: Manual ID generation in UI layer!
    public static Long getNextCustomerId() {
        Long maxId = 0L;
        for (Customer c : allCustomers) {
            if (c.getId() > maxId) {
                maxId = c.getId();
            }
        }
        return maxId + 1;
    }
    
    public static Long getNextOrderId() {
        Long maxId = 0L;
        for (Order o : allOrders) {
            if (o.getId() > maxId) {
                maxId = o.getId();
            }
        }
        return maxId + 1;
    }
    
    public static void main(String[] args) {
        // Use system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignore, will use default
        }
        
        // Create and show UI on EDT
        SwingUtilities.invokeLater(() -> {
            MainApp app = new MainApp();
            app.setVisible(true);
        });
    }
}
