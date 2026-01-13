package aim.legacy.ui;

import aim.legacy.domain.Customer;
import aim.legacy.domain.Order;
import aim.legacy.domain.OrderLine;
import aim.legacy.domain.Product;
import aim.legacy.services.OrderService;
import aim.legacy.services.PricingCalculator;
import aim.legacy.services.ValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * OrderEditorDialog - dialog for creating/editing orders.
 * Legacy pattern: Complex modal dialog with tight coupling between UI and business logic.
 * Intentionally demonstrates legacy patterns like manual field updates and duplicated logic.
 */
public class OrderEditorDialog extends JDialog {

    private final OrderService orderService;
    private Order order;
    private boolean saved = false;
    
    private JComboBox<CustomerItem> customerCombo;
    private JTable linesTable;
    private DefaultTableModel linesTableModel;
    
    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;
    private JTextArea statusArea;
    
    public OrderEditorDialog(Frame parent, OrderService orderService, Order order) {
        super(parent, order == null ? "New Order" : "Edit Order", true);
        this.orderService = orderService;
        this.order = order != null ? order : new Order();
        
        setupUI();
        populateCustomers();
        populateFields();
        
        setSize(800, 600);
        setLocationRelativeTo(parent);
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Top panel - customer selection
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Customer:"));
        customerCombo = new JComboBox<>();
        customerCombo.setPreferredSize(new Dimension(300, 25));
        topPanel.add(customerCombo);
        add(topPanel, BorderLayout.NORTH);
        
        // Center - lines table and totals
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        // Lines table
        String[] columns = {"ID", "Product", "Quantity", "Unit Price", "Line Total"};
        linesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        linesTable = new JTable(linesTableModel);
        JScrollPane scrollPane = new JScrollPane(linesTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Line buttons
        JPanel lineButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addLineButton = new JButton("Add Line");
        addLineButton.addActionListener(e -> addLine());
        lineButtonPanel.add(addLineButton);
        
        JButton editLineButton = new JButton("Edit Line");
        editLineButton.addActionListener(e -> editLine());
        lineButtonPanel.add(editLineButton);
        
        JButton removeLineButton = new JButton("Remove Line");
        removeLineButton.addActionListener(e -> removeLine());
        lineButtonPanel.add(removeLineButton);
        
        centerPanel.add(lineButtonPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Right panel - totals and status
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        JPanel totalsPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        totalsPanel.setBorder(BorderFactory.createTitledBorder("Totals"));
        totalsPanel.add(new JLabel("Subtotal:"));
        subtotalLabel = new JLabel("$0.00");
        totalsPanel.add(subtotalLabel);
        totalsPanel.add(new JLabel("Discount:"));
        discountLabel = new JLabel("$0.00");
        totalsPanel.add(discountLabel);
        totalsPanel.add(new JLabel("Tax:"));
        taxLabel = new JLabel("$0.00");
        totalsPanel.add(taxLabel);
        totalsPanel.add(new JLabel("Total:"));
        totalLabel = new JLabel("$0.00");
        Font boldFont = totalLabel.getFont().deriveFont(Font.BOLD, 14f);
        totalLabel.setFont(boldFont);
        totalsPanel.add(totalLabel);
        
        rightPanel.add(totalsPanel, BorderLayout.NORTH);
        
        // Status area
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
        statusArea = new JTextArea(5, 20);
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusPanel.add(statusScroll, BorderLayout.CENTER);
        
        rightPanel.add(statusPanel, BorderLayout.CENTER);
        
        add(rightPanel, BorderLayout.EAST);
        
        // Bottom panel - action buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> save());
        bottomPanel.add(saveButton);
        
        JButton reloadButton = new JButton("Reload");
        reloadButton.addActionListener(e -> reload());
        bottomPanel.add(reloadButton);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> cancel());
        bottomPanel.add(cancelButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void populateCustomers() {
        customerCombo.removeAllItems();
        List<Customer> customers = orderService.getAllCustomers();
        for (Customer customer : customers) {
            customerCombo.addItem(new CustomerItem(customer));
        }
    }
    
    private void populateFields() {
        if (order.getCustomerId() != null) {
            for (int i = 0; i < customerCombo.getItemCount(); i++) {
                CustomerItem item = customerCombo.getItemAt(i);
                if (item.customer.getId().equals(order.getCustomerId())) {
                    customerCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        refreshLines();
        updateTotals();
    }
    
    private void refreshLines() {
        linesTableModel.setRowCount(0);
        for (OrderLine line : order.getLines()) {
            linesTableModel.addRow(new Object[]{
                line.getId(),
                line.getProductName(),
                line.getQuantity(),
                "$" + line.getUnitPrice(),
                "$" + line.getLineTotal()
            });
        }
    }
    
    private void updateTotals() {
        // Recalculate pricing
        PricingCalculator.calculateOrderPricing(order);
        
        // Update labels
        subtotalLabel.setText("$" + order.getSubtotal());
        discountLabel.setText("$" + order.getDiscount());
        taxLabel.setText("$" + order.getTax());
        totalLabel.setText("$" + order.getTotal());
    }
    
    private void addLine() {
        OrderLineDialog dialog = new OrderLineDialog(this, orderService, null);
        dialog.setVisible(true);
        
        OrderLine line = dialog.getOrderLine();
        if (line != null) {
            order.addLine(line);
            refreshLines();
            updateTotals();
        }
    }
    
    private void editLine() {
        int selectedRow = linesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a line to edit");
            return;
        }
        
        OrderLine line = order.getLines().get(selectedRow);
        OrderLineDialog dialog = new OrderLineDialog(this, orderService, line);
        dialog.setVisible(true);
        
        OrderLine updatedLine = dialog.getOrderLine();
        if (updatedLine != null) {
            order.getLines().set(selectedRow, updatedLine);
            refreshLines();
            updateTotals();
        }
    }
    
    private void removeLine() {
        int selectedRow = linesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a line to remove");
            return;
        }
        
        order.getLines().remove(selectedRow);
        refreshLines();
        updateTotals();
    }
    
    private void save() {
        // Update customer
        CustomerItem selectedCustomer = (CustomerItem) customerCombo.getSelectedItem();
        if (selectedCustomer == null) {
            JOptionPane.showMessageDialog(this, "Please select a customer");
            return;
        }
        
        order.setCustomerId(selectedCustomer.customer.getId());
        order.setCustomerName(selectedCustomer.customer.getName());
        
        // Save order
        try {
            orderService.saveOrder(order);
            statusArea.setText("Order saved successfully");
            saved = true;
            
            // Close after brief delay
            Timer timer = new Timer(500, e -> dispose());
            timer.setRepeats(false);
            timer.start();
            
        } catch (ValidationException e) {
            StringBuilder sb = new StringBuilder("Validation errors:\n");
            for (String error : e.getErrors()) {
                sb.append("- ").append(error).append("\n");
            }
            statusArea.setText(sb.toString());
            JOptionPane.showMessageDialog(this, sb.toString(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void reload() {
        if (order.getId() != null) {
            Order reloaded = orderService.getOrder(order.getId());
            if (reloaded != null) {
                this.order = reloaded;
                populateFields();
                statusArea.setText("Order reloaded from file");
            }
        }
    }
    
    private void cancel() {
        saved = false;
        dispose();
    }
    
    public boolean isSaved() {
        return saved;
    }
    
    // Helper class for customer combobox
    private static class CustomerItem {
        final Customer customer;
        
        CustomerItem(Customer customer) {
            this.customer = customer;
        }
        
        @Override
        public String toString() {
            return customer.getName();
        }
    }
}
