package aim.legacy.ui;

import aim.legacy.domain.Customer;
import aim.legacy.services.OrderService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * CustomersScreen - screen for managing customers.
 * Legacy pattern: Manual UI construction with tight coupling to service.
 */
public class CustomersScreen extends JPanel {

    private final OrderService orderService;
    
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    public CustomersScreen(OrderService orderService) {
        this.orderService = orderService;
        setupUI();
        loadCustomers();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Top panel - search
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        topPanel.add(searchField);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchCustomers());
        topPanel.add(searchButton);
        JButton clearButton = new JButton("Show All");
        clearButton.addActionListener(e -> loadCustomers());
        topPanel.add(clearButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Center - table
        String[] columns = {"ID", "Name", "Email", "Phone", "Address"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customerTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(customerTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel - buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Customer");
        addButton.addActionListener(e -> addCustomer());
        buttonPanel.add(addButton);
        
        JButton editButton = new JButton("Edit Customer");
        editButton.addActionListener(e -> editCustomer());
        buttonPanel.add(editButton);
        
        JButton deleteButton = new JButton("Delete Customer");
        deleteButton.addActionListener(e -> deleteCustomer());
        buttonPanel.add(deleteButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public void refresh() {
        loadCustomers();
    }
    
    private void loadCustomers() {
        tableModel.setRowCount(0);
        List<Customer> customers = orderService.getAllCustomers();
        for (Customer customer : customers) {
            tableModel.addRow(new Object[]{
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress()
            });
        }
    }
    
    private void searchCustomers() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadCustomers();
            return;
        }
        
        tableModel.setRowCount(0);
        List<Customer> customers = orderService.searchCustomers(query);
        for (Customer customer : customers) {
            tableModel.addRow(new Object[]{
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress()
            });
        }
    }
    
    private void addCustomer() {
        CustomerDialog dialog = new CustomerDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        
        Customer customer = dialog.getCustomer();
        if (customer != null) {
            orderService.saveCustomer(customer);
            loadCustomers();
        }
    }
    
    private void editCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a customer to edit");
            return;
        }
        
        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        Customer customer = orderService.getCustomer(id);
        
        CustomerDialog dialog = new CustomerDialog((Frame) SwingUtilities.getWindowAncestor(this), customer);
        dialog.setVisible(true);
        
        Customer updatedCustomer = dialog.getCustomer();
        if (updatedCustomer != null) {
            orderService.saveCustomer(updatedCustomer);
            loadCustomers();
        }
    }
    
    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this customer?",
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Long id = (Long) tableModel.getValueAt(selectedRow, 0);
            orderService.deleteCustomer(id);
            loadCustomers();
        }
    }
}
