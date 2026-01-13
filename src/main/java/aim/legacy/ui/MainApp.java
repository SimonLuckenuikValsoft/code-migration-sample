package aim.legacy.ui;

import aim.legacy.services.OrderService;

import javax.swing.*;
import java.awt.*;

/**
 * MainApp - main entry point for the desktop application.
 * Legacy pattern: Simple Swing JFrame with card layout for navigation.
 */
public class MainApp extends JFrame {

    private static final String DATA_PATH = "data";
    
    private OrderService orderService;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    private CustomersScreen customersScreen;
    private OrdersScreen ordersScreen;
    
    public MainApp() {
        super("Order Entry System - Legacy Edition");
        
        // Initialize service
        orderService = OrderService.getInstance(DATA_PATH);
        
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
        customersScreen = new CustomersScreen(orderService);
        ordersScreen = new OrdersScreen(orderService, this);
        
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
        reloadItem.addActionListener(e -> reloadData());
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
    
    private void reloadData() {
        orderService.reloadData();
        customersScreen.refresh();
        ordersScreen.refresh();
        JOptionPane.showMessageDialog(this, "Data reloaded successfully");
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
