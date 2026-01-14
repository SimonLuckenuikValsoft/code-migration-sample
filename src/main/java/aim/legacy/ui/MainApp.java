package aim.legacy.ui;

import aim.legacy.db.DB;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class MainApp extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    private CustomersScreen customersScreen;
    private OrdersScreen ordersScreen;
    
    static {
        DB.getConn();
    }
    
    public MainApp() {
        super("Order Entry System");
        
        setupUI();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
    }
    
    private void setupUI() {
        setJMenuBar(createMenuBar());
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        customersScreen = new CustomersScreen(this);
        ordersScreen = new OrdersScreen(this);
        
        mainPanel.add(customersScreen, "customers");
        mainPanel.add(ordersScreen, "orders");
        
        add(mainPanel);
        
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
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> {
            DB.closeConn();
            System.exit(0);
        });
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
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        
        SwingUtilities.invokeLater(() -> {
            MainApp app = new MainApp();
            app.setVisible(true);
        });
    }
}
