package aim.legacy.ui;

import aim.legacy.domain.Customer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * CustomersScreen - screen for managing customers.
 * TECHNICAL DEBT: Direct access to global state, no abstraction
 */
public class CustomersScreen extends JPanel {

    private final MainApp mainApp;
    
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    
    public CustomersScreen(MainApp mainApp) {
        this.mainApp = mainApp;
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
    
    // TECHNICAL DEBT: Direct access to static list
    private void loadCustomers() {
        tableModel.setRowCount(0);
        for (Customer customer : MainApp.allCustomers) {
            tableModel.addRow(new Object[]{
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress()
            });
        }
    }
    
    // TECHNICAL DEBT: Business logic in UI - manual search implementation
    private void searchCustomers() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadCustomers();
            return;
        }
        
        tableModel.setRowCount(0);
        String lowerQuery = query.toLowerCase();
        for (Customer customer : MainApp.allCustomers) {
            if (customer.getName().toLowerCase().contains(lowerQuery)) {
                tableModel.addRow(new Object[]{
                    customer.getId(),
                    customer.getName(),
                    customer.getEmail(),
                    customer.getPhone(),
                    customer.getAddress()
                });
            }
        }
    }
    
    private void addCustomer() {
        CustomerDialog dialog = new CustomerDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        
        Customer customer = dialog.getCustomer();
        if (customer != null) {
            // TECHNICAL DEBT: Direct manipulation of global state
            customer.setId(MainApp.getNextCustomerId());
            MainApp.allCustomers.add(customer);
            MainApp.saveCustomers();
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
        
        // TECHNICAL DEBT: Linear search through list
        Customer customer = null;
        for (Customer c : MainApp.allCustomers) {
            if (c.getId().equals(id)) {
                customer = c;
                break;
            }
        }
        
        if (customer == null) return;
        
        CustomerDialog dialog = new CustomerDialog((Frame) SwingUtilities.getWindowAncestor(this), customer);
        dialog.setVisible(true);
        
        Customer updatedCustomer = dialog.getCustomer();
        if (updatedCustomer != null) {
            // TECHNICAL DEBT: Replace in place
            for (int i = 0; i < MainApp.allCustomers.size(); i++) {
                if (MainApp.allCustomers.get(i).getId().equals(id)) {
                    MainApp.allCustomers.set(i, updatedCustomer);
                    break;
                }
            }
            MainApp.saveCustomers();
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
            
            // TECHNICAL DEBT: Remove from global list directly
            MainApp.allCustomers.removeIf(c -> c.getId().equals(id));
            MainApp.saveCustomers();
            loadCustomers();
        }
    }
}
