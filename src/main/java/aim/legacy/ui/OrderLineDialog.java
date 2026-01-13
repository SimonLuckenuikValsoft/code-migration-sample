package aim.legacy.ui;

import aim.legacy.domain.OrderLine;
import aim.legacy.domain.Product;
import aim.legacy.services.OrderService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * OrderLineDialog - dialog for adding/editing order lines.
 * Legacy pattern: Simple modal dialog with form fields.
 */
public class OrderLineDialog extends JDialog {

    private final OrderService orderService;
    private OrderLine orderLine;
    private boolean confirmed = false;
    
    private JComboBox<ProductItem> productCombo;
    private JSpinner quantitySpinner;
    private JTextField priceField;
    
    public OrderLineDialog(Dialog parent, OrderService orderService, OrderLine orderLine) {
        super(parent, orderLine == null ? "Add Line" : "Edit Line", true);
        this.orderService = orderService;
        this.orderLine = orderLine;
        
        setupUI();
        populateProducts();
        populateFields();
        
        setSize(400, 200);
        setLocationRelativeTo(parent);
    }
    
    private void setupUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Product
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Product:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        productCombo = new JComboBox<>();
        productCombo.addActionListener(e -> updatePriceFromProduct());
        panel.add(productCombo, gbc);
        
        // Quantity
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        panel.add(quantitySpinner, gbc);
        
        // Unit Price
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Unit Price:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        priceField = new JTextField(10);
        panel.add(priceField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> save());
        buttonPanel.add(okButton);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> cancel());
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);
        
        add(panel);
    }
    
    private void populateProducts() {
        productCombo.removeAllItems();
        List<Product> products = orderService.getAllProducts();
        for (Product product : products) {
            productCombo.addItem(new ProductItem(product));
        }
    }
    
    private void populateFields() {
        if (orderLine != null) {
            // Select product
            for (int i = 0; i < productCombo.getItemCount(); i++) {
                ProductItem item = productCombo.getItemAt(i);
                if (item.product.getId().equals(orderLine.getProductId())) {
                    productCombo.setSelectedIndex(i);
                    break;
                }
            }
            
            quantitySpinner.setValue(orderLine.getQuantity());
            priceField.setText(orderLine.getUnitPrice().toString());
        } else {
            updatePriceFromProduct();
        }
    }
    
    private void updatePriceFromProduct() {
        ProductItem selected = (ProductItem) productCombo.getSelectedItem();
        if (selected != null) {
            priceField.setText(selected.product.getUnitPrice().toString());
        }
    }
    
    private void save() {
        // Validate
        ProductItem selectedProduct = (ProductItem) productCombo.getSelectedItem();
        if (selectedProduct == null) {
            JOptionPane.showMessageDialog(this, "Please select a product");
            return;
        }
        
        int quantity = (Integer) quantitySpinner.getValue();
        BigDecimal unitPrice;
        try {
            unitPrice = new BigDecimal(priceField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price format");
            return;
        }
        
        // Create or update line
        if (orderLine == null) {
            orderLine = new OrderLine();
        }
        
        orderLine.setProductId(selectedProduct.product.getId());
        orderLine.setProductName(selectedProduct.product.getName());
        orderLine.setQuantity(quantity);
        orderLine.setUnitPrice(unitPrice);
        
        confirmed = true;
        dispose();
    }
    
    private void cancel() {
        confirmed = false;
        orderLine = null;
        dispose();
    }
    
    public OrderLine getOrderLine() {
        return confirmed ? orderLine : null;
    }
    
    // Helper class for product combobox
    private static class ProductItem {
        final Product product;
        
        ProductItem(Product product) {
            this.product = product;
        }
        
        @Override
        public String toString() {
            return product.getName() + " - $" + product.getUnitPrice();
        }
    }
}
