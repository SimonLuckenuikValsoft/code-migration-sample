package aim.legacy.ui;

import aim.legacy.domain.Customer;

import javax.swing.*;
import java.awt.*;

/**
 * CustomerDialog - dialog for adding/editing customers.
 * Legacy pattern: Simple modal dialog with form fields.
 */
public class CustomerDialog extends JDialog {

    private Customer customer;
    private boolean confirmed = false;
    
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField addressField;
    
    public CustomerDialog(Frame parent, Customer customer) {
        super(parent, customer == null ? "Add Customer" : "Edit Customer", true);
        this.customer = customer;
        
        setupUI();
        populateFields();
        
        setSize(400, 250);
        setLocationRelativeTo(parent);
    }
    
    private void setupUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        panel.add(nameField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        emailField = new JTextField(20);
        panel.add(emailField, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        phoneField = new JTextField(20);
        panel.add(phoneField, gbc);
        
        // Address
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        addressField = new JTextField(20);
        panel.add(addressField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> save());
        buttonPanel.add(saveButton);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> cancel());
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);
        
        add(panel);
    }
    
    private void populateFields() {
        if (customer != null) {
            nameField.setText(customer.getName());
            emailField.setText(customer.getEmail());
            phoneField.setText(customer.getPhone());
            addressField.setText(customer.getAddress());
        }
    }
    
    private void save() {
        // Basic validation
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required");
            return;
        }
        
        if (customer == null) {
            customer = new Customer();
        }
        
        customer.setName(nameField.getText().trim());
        customer.setEmail(emailField.getText().trim());
        customer.setPhone(phoneField.getText().trim());
        customer.setAddress(addressField.getText().trim());
        
        confirmed = true;
        dispose();
    }
    
    private void cancel() {
        confirmed = false;
        customer = null;
        dispose();
    }
    
    public Customer getCustomer() {
        return confirmed ? customer : null;
    }
}
