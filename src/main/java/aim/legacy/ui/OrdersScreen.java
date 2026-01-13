package aim.legacy.ui;

import aim.legacy.domain.Order;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * OrdersScreen - screen for managing orders.
 */
public class OrdersScreen extends JPanel {

    private final MainApp mainApp;
    
    private JTable orderTable;
    private DefaultTableModel tableModel;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public OrdersScreen(MainApp mainApp) {
        this.mainApp = mainApp;
        setupUI();
        loadOrders();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Top panel - title
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Orders"));
        add(topPanel, BorderLayout.NORTH);
        
        // Center - table
        String[] columns = {"ID", "Customer", "Date", "Subtotal", "Discount", "Tax", "Total"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(orderTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel - buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton newButton = new JButton("New Order");
        newButton.addActionListener(e -> createOrder());
        buttonPanel.add(newButton);
        
        JButton editButton = new JButton("Edit Order");
        editButton.addActionListener(e -> editOrder());
        buttonPanel.add(editButton);
        
        JButton deleteButton = new JButton("Delete Order");
        deleteButton.addActionListener(e -> deleteOrder());
        buttonPanel.add(deleteButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public void refresh() {
        loadOrders();
    }
    
    private void loadOrders() {
        tableModel.setRowCount(0);
        for (Order order : MainApp.allOrders) {
            tableModel.addRow(new Object[]{
                order.getId(),
                order.getCustomerName(),
                order.getOrderDate() != null ? order.getOrderDate().format(DATE_FORMAT) : "",
                "$" + order.getSubtotal(),
                "$" + order.getDiscount(),
                "$" + order.getTax(),
                "$" + order.getTotal()
            });
        }
    }
    
    private void createOrder() {
        OrderEditorDialog dialog = new OrderEditorDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), 
            null);
        dialog.setVisible(true);
        
        if (dialog.isSaved()) {
            loadOrders();
        }
    }
    
    private void editOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order to edit");
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        
        Order order = null;
        for (Order o : MainApp.allOrders) {
            if (o.getId().equals(id)) {
                order = o;
                break;
            }
        }
        
        if (order == null) return;
        
        OrderEditorDialog dialog = new OrderEditorDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), 
            order);
        dialog.setVisible(true);
        
        if (dialog.isSaved()) {
            loadOrders();
        }
    }
    
    private void deleteOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order to delete");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this order?",
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Long id = (Long) tableModel.getValueAt(selectedRow, 0);
            
            MainApp.allOrders.removeIf(o -> o.getId().equals(id));
            MainApp.saveOrders();
            loadOrders();
        }
    }
}
