package aim.legacy.ui;

import aim.legacy.domain.Customer;
import aim.legacy.domain.Order;
import aim.legacy.domain.OrderLine;
import aim.legacy.domain.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderEditorDialog - dialog for creating/editing orders.
 * 
 * TECHNICAL DEBT WARNING:
 * This class is a mess:
 * - Business logic (pricing, validation) mixed with UI code
 * - Duplicated calculations in multiple places
 * - No separation between calculation and display
 * - Hard-coded business rules scattered throughout
 * - Direct manipulation of domain objects
 */
public class OrderEditorDialog extends JDialog {

    private Order order;
    private boolean saved = false;
    
    private JComboBox<String> customerCombo;
    private JTable linesTable;
    private DefaultTableModel linesTableModel;
    
    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;
    private JTextArea statusArea;
    
    // TECHNICAL DEBT: Hard-coded constants - should be configurable
    private static final BigDecimal TAX_RATE = new BigDecimal("0.14975");
    
    public OrderEditorDialog(Frame parent, Order order) {
        super(parent, order == null ? "New Order" : "Edit Order", true);
        this.order = order != null ? order : new Order();
        
        if (this.order.getLines() == null) {
            this.order.setLines(new ArrayList<>());
        }
        
        setupUI();
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
        String[] columns = {"Product", "Quantity", "Unit Price", "Line Total"};
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
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> cancel());
        bottomPanel.add(cancelButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void populateFields() {
        // Load customers into combo
        customerCombo.removeAllItems();
        for (Customer c : MainApp.allCustomers) {
            customerCombo.addItem(c.getName());
        }
        
        if (order.getCustomerId() != null) {
            // TECHNICAL DEBT: Linear search to find customer
            for (Customer c : MainApp.allCustomers) {
                if (c.getId().equals(order.getCustomerId())) {
                    customerCombo.setSelectedItem(c.getName());
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
            // TECHNICAL DEBT: Duplicate calculation of line total
            BigDecimal lineTotal = line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
            linesTableModel.addRow(new Object[]{
                line.getProductName(),
                line.getQuantity(),
                "$" + line.getUnitPrice(),
                "$" + lineTotal.setScale(2, RoundingMode.HALF_UP)
            });
        }
    }
    
    // TECHNICAL DEBT: Business logic embedded in UI!
    // This should be in a separate service layer
    private void updateTotals() {
        // Calculate subtotal
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderLine line : order.getLines()) {
            BigDecimal lineTotal = line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
            subtotal = subtotal.add(lineTotal);
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        
        // Calculate discount based on tiers
        BigDecimal discount = BigDecimal.ZERO;
        if (subtotal.compareTo(new BigDecimal("2000")) >= 0) {
            discount = subtotal.multiply(new BigDecimal("0.15"));
        } else if (subtotal.compareTo(new BigDecimal("1000")) >= 0) {
            discount = subtotal.multiply(new BigDecimal("0.10"));
        } else if (subtotal.compareTo(new BigDecimal("500")) >= 0) {
            discount = subtotal.multiply(new BigDecimal("0.05"));
        }
        discount = discount.setScale(2, RoundingMode.HALF_UP);
        
        // Calculate tax
        BigDecimal taxableAmount = subtotal.subtract(discount);
        BigDecimal tax = taxableAmount.multiply(TAX_RATE);
        tax = tax.setScale(2, RoundingMode.HALF_UP);
        
        // Calculate total
        BigDecimal total = subtotal.subtract(discount).add(tax);
        total = total.setScale(2, RoundingMode.HALF_UP);
        
        // Update order object
        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setTax(tax);
        order.setTotal(total);
        
        // Update labels
        subtotalLabel.setText("$" + subtotal);
        discountLabel.setText("$" + discount);
        taxLabel.setText("$" + tax);
        totalLabel.setText("$" + total);
    }
    
    private void addLine() {
        // Show product selection dialog
        String[] productNames = new String[MainApp.allProducts.size()];
        for (int i = 0; i < MainApp.allProducts.size(); i++) {
            Product p = MainApp.allProducts.get(i);
            productNames[i] = p.getName() + " - $" + p.getUnitPrice();
        }
        
        String selected = (String) JOptionPane.showInputDialog(
            this,
            "Select product:",
            "Add Line",
            JOptionPane.QUESTION_MESSAGE,
            null,
            productNames,
            productNames[0]
        );
        
        if (selected == null) return;
        
        // Find the product
        Product selectedProduct = null;
        for (Product p : MainApp.allProducts) {
            if (selected.startsWith(p.getName())) {
                selectedProduct = p;
                break;
            }
        }
        
        if (selectedProduct == null) return;
        
        // Get quantity
        String qtyStr = JOptionPane.showInputDialog(this, "Enter quantity:", "1");
        if (qtyStr == null) return;
        
        int quantity = 1;
        try {
            quantity = Integer.parseInt(qtyStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity");
            return;
        }
        
        // Create line
        OrderLine line = new OrderLine();
        line.setProductId(selectedProduct.getId());
        line.setProductName(selectedProduct.getName());
        line.setQuantity(quantity);
        line.setUnitPrice(selectedProduct.getUnitPrice());
        
        order.getLines().add(line);
        refreshLines();
        updateTotals();
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
    
    // TECHNICAL DEBT: Validation logic embedded in UI save method!
    private void save() {
        // Get selected customer
        String customerName = (String) customerCombo.getSelectedItem();
        if (customerName == null) {
            JOptionPane.showMessageDialog(this, "Please select a customer");
            return;
        }
        
        // Find customer
        Customer customer = null;
        for (Customer c : MainApp.allCustomers) {
            if (c.getName().equals(customerName)) {
                customer = c;
                break;
            }
        }
        
        if (customer == null) {
            JOptionPane.showMessageDialog(this, "Customer not found");
            return;
        }
        
        // Validate order
        List<String> errors = new ArrayList<>();
        
        if (order.getLines().isEmpty()) {
            errors.add("Order must have at least one line item");
        }
        
        for (int i = 0; i < order.getLines().size(); i++) {
            OrderLine line = order.getLines().get(i);
            if (line.getQuantity() <= 0) {
                errors.add("Line " + (i + 1) + ": Quantity must be positive");
            }
            if (line.getUnitPrice() == null || line.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                errors.add("Line " + (i + 1) + ": Unit price must be zero or greater");
            }
        }
        
        // Check discount doesn't exceed 15%
        if (order.getSubtotal().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountRate = order.getDiscount().divide(order.getSubtotal(), 4, RoundingMode.HALF_UP);
            if (discountRate.compareTo(new BigDecimal("0.15")) > 0) {
                errors.add("Discount cannot exceed 15%");
            }
        }
        
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder("Validation errors:\n");
            for (String error : errors) {
                sb.append("- ").append(error).append("\n");
            }
            statusArea.setText(sb.toString());
            JOptionPane.showMessageDialog(this, sb.toString(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Update order
        order.setCustomerId(customer.getId());
        order.setCustomerName(customer.getName());
        
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        
        // Save order
        if (order.getId() == null) {
            order.setId(MainApp.getNextOrderId());
            
            // Generate line IDs
            long lineId = 1;
            for (OrderLine line : order.getLines()) {
                if (line.getId() == null) {
                    line.setId(lineId++);
                }
            }
            
            MainApp.allOrders.add(order);
        } else {
            // Update existing
            for (int i = 0; i < MainApp.allOrders.size(); i++) {
                if (MainApp.allOrders.get(i).getId().equals(order.getId())) {
                    MainApp.allOrders.set(i, order);
                    break;
                }
            }
        }
        
        MainApp.saveOrders();
        statusArea.setText("Order saved successfully");
        saved = true;
        
        // Close after brief delay
        Timer timer = new Timer(500, e -> dispose());
        timer.setRepeats(false);
        timer.start();
    }
    
    private void cancel() {
        saved = false;
        dispose();
    }
    
    public boolean isSaved() {
        return saved;
    }
}
