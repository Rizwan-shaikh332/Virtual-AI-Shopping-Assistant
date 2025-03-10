import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import java.io.*;
import org.json.*;

public class ShoppingAssistantGUI extends JFrame {
    private JTextField searchField;
    private JComboBox<String> categoryComboBox;
    private JComboBox<String> brandComboBox;
    private JSlider priceSlider;
    private JSlider ratingSlider;
    private JPanel resultsPanel;
    private JScrollPane scrollPane;
    private JLabel statusLabel;
    
    private final String API_BASE_URL = "http://localhost:5000/api";
    private ArrayList<String> categories = new ArrayList<>();
    private ArrayList<String> brands = new ArrayList<>();
    private double maxPrice = 1000.0;
    
    public ShoppingAssistantGUI() {
        setTitle("Shopping Assistant");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main layout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Search panel
        JPanel searchPanel = createSearchPanel();
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Filter panel on the left
        JPanel filterPanel = createFilterPanel();
        mainPanel.add(filterPanel, BorderLayout.WEST);
        
        // Results panel in the center
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Status bar at the bottom
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Load initial data
        loadProductData();
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        searchField = new JTextField(30);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> performSearch());
        
        // Add action listener to search field for Enter key
        searchField.addActionListener(e -> performSearch());
        
        panel.add(new JLabel("Search: "), BorderLayout.WEST);
        panel.add(searchField, BorderLayout.CENTER);
        panel.add(searchButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new CompoundBorder(
                new TitledBorder("Filters"),
                new EmptyBorder(5, 5, 5, 5)));
        panel.setPreferredSize(new Dimension(200, 500));
        
        // Category filter
        JPanel categoryPanel = new JPanel(new BorderLayout(5, 0));
        categoryPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        categoryPanel.add(new JLabel("Category:"), BorderLayout.NORTH);
        
        categoryComboBox = new JComboBox<>();
        categoryComboBox.addItem("All Categories");
        categoryPanel.add(categoryComboBox, BorderLayout.CENTER);
        
        // Brand filter
        JPanel brandPanel = new JPanel(new BorderLayout(5, 0));
        brandPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        brandPanel.add(new JLabel("Brand:"), BorderLayout.NORTH);
        
        brandComboBox = new JComboBox<>();
        brandComboBox.addItem("All Brands");
        brandPanel.add(brandComboBox, BorderLayout.CENTER);
        
        // Price filter
        JPanel pricePanel = new JPanel(new BorderLayout(5, 0));
        pricePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        pricePanel.add(new JLabel("Min Price:"), BorderLayout.NORTH);
        
        priceSlider = new JSlider(0, 100, 0);
        priceSlider.setMajorTickSpacing(25);
        priceSlider.setPaintTicks(true);
        priceSlider.setPaintLabels(true);
        
        Dictionary<Integer, JLabel> priceLabels = new Hashtable<>();
        priceLabels.put(0, new JLabel("$0"));
        priceLabels.put(25, new JLabel("$250"));
        priceLabels.put(50, new JLabel("$500"));
        priceLabels.put(75, new JLabel("$750"));
        priceLabels.put(100, new JLabel("$1000+"));
        priceSlider.setLabelTable(priceLabels);
        
        pricePanel.add(priceSlider, BorderLayout.CENTER);
        
        // Rating filter
        JPanel ratingPanel = new JPanel(new BorderLayout(5, 0));
        ratingPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        ratingPanel.add(new JLabel("Min Rating:"), BorderLayout.NORTH);
        
        ratingSlider = new JSlider(0, 50, 0);
        ratingSlider.setMajorTickSpacing(10);
        ratingSlider.setPaintTicks(true);
        ratingSlider.setPaintLabels(true);
        
        Dictionary<Integer, JLabel> ratingLabels = new Hashtable<>();
        ratingLabels.put(0, new JLabel("0"));
        ratingLabels.put(10, new JLabel("1"));
        ratingLabels.put(20, new JLabel("2"));
        ratingLabels.put(30, new JLabel("3"));
        ratingLabels.put(40, new JLabel("4"));
        ratingLabels.put(50, new JLabel("5"));
        ratingSlider.setLabelTable(ratingLabels);
        
        ratingPanel.add(ratingSlider, BorderLayout.CENTER);
        
        // Apply filters button
        JButton applyButton = new JButton("Apply Filters");
        applyButton.addActionListener(e -> performSearch());
        
        // Reset filters button
        JButton resetButton = new JButton("Reset Filters");
        resetButton.addActionListener(e -> resetFilters());
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        buttonPanel.add(resetButton);
        buttonPanel.add(applyButton);
        
        panel.add(categoryPanel);
        panel.add(brandPanel);
        panel.add(pricePanel);
        panel.add(ratingPanel);
        panel.add(buttonPanel);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private void loadProductData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    statusLabel.setText("Loading product data...");
                    
                    // Load all products to extract categories and brands
                    URL url = new URL(API_BASE_URL + "/products");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    
                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        
                        // Parse JSON response
                        JSONArray products = new JSONArray(response.toString());
                        
                        // Extract unique categories and brands
                        HashSet<String> categorySet = new HashSet<>();
                        HashSet<String> brandSet = new HashSet<>();
                        double highestPrice = 0.0;
                        
                        for (int i = 0; i < products.length(); i++) {
                            JSONObject product = products.getJSONObject(i);
                            
                            if (product.has("category") && !product.isNull("category")) {
                                categorySet.add(product.getString("category"));
                            }
                            
                            if (product.has("brand") && !product.isNull("brand")) {
                                brandSet.add(product.getString("brand"));
                            }
                            
                            if (product.has("price") && !product.isNull("price")) {
                                double price = product.getDouble("price");
                                if (price > highestPrice) {
                                    highestPrice = price;
                                }
                            }
                        }
                        
                        // Update UI with categories and brands
                        categories = new ArrayList<>(categorySet);
                        brands = new ArrayList<>(brandSet);
                        Collections.sort(categories);
                        Collections.sort(brands);
                        
                        if (highestPrice > 0) {
                            maxPrice = Math.ceil(highestPrice / 100) * 100;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            
            @Override
            protected void done() {
                // Update UI components with the loaded data
                categoryComboBox.removeAllItems();
                categoryComboBox.addItem("All Categories");
                for (String category : categories) {
                    categoryComboBox.addItem(category);
                }
                
                brandComboBox.removeAllItems();
                brandComboBox.addItem("All Brands");
                for (String brand : brands) {
                    brandComboBox.addItem(brand);
                }
                
                // Update price slider with the actual max price
                updatePriceSlider();
                
                statusLabel.setText("Ready");
            }
        };
        
        worker.execute();
    }
    
    private void updatePriceSlider() {
        Dictionary<Integer, JLabel> priceLabels = new Hashtable<>();
        priceLabels.put(0, new JLabel("$0"));
        priceLabels.put(25, new JLabel("$" + (int)(maxPrice * 0.25)));
        priceLabels.put(50, new JLabel("$" + (int)(maxPrice * 0.5)));
        priceLabels.put(75, new JLabel("$" + (int)(maxPrice * 0.75)));
        priceLabels.put(100, new JLabel("$" + (int)maxPrice + "+"));
        priceSlider.setLabelTable(priceLabels);
    }
    
    private void performSearch() {
        String query = searchField.getText().trim();
        
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a search term", 
                "Search Error", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        SwingWorker<ArrayList<JSONObject>, Void> worker = new SwingWorker<ArrayList<JSONObject>, Void>() {
            @Override
            protected ArrayList<JSONObject> doInBackground() throws Exception {
                statusLabel.setText("Searching...");
                ArrayList<JSONObject> results = new ArrayList<>();
                
                try {
                    // Build query string with filters
                    StringBuilder urlBuilder = new StringBuilder(API_BASE_URL + "/query?query=" + URLEncoder.encode(query, "UTF-8"));
                    
                    // Add category filter
                    String selectedCategory = (String) categoryComboBox.getSelectedItem();
                    if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
                        urlBuilder.append("&category=").append(URLEncoder.encode(selectedCategory, "UTF-8"));
                    }
                    
                    // Add brand filter
                    String selectedBrand = (String) brandComboBox.getSelectedItem();
                    if (selectedBrand != null && !selectedBrand.equals("All Brands")) {
                        urlBuilder.append("&brand=").append(URLEncoder.encode(selectedBrand, "UTF-8"));
                    }
                    
                    // Add price filter
                    int priceValue = priceSlider.getValue();
                    if (priceValue > 0) {
                        double minPrice = (priceValue / 100.0) * maxPrice;
                        urlBuilder.append("&min_price=").append(minPrice);
                    }
                    
                    // Add rating filter
                    int ratingValue = ratingSlider.getValue();
                    if (ratingValue > 0) {
                        double minRating = ratingValue / 10.0;
                        urlBuilder.append("&min_rating=").append(minRating);
                    }
                    
                    URL url = new URL(urlBuilder.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    
                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        
                        // Parse JSON response
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        JSONArray products = jsonResponse.getJSONArray("products");
                        
                        for (int i = 0; i < products.length(); i++) {
                            results.add(products.getJSONObject(i));
                        }
                    } else {
                        System.out.println("Error response: " + responseCode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                return results;
            }
            
            @Override
            protected void done() {
                try {
                    ArrayList<JSONObject> results = get();
                    displayResults(results);
                    statusLabel.setText("Found " + results.size() + " results");
                } catch (Exception e) {
                    e.printStackTrace();
                    statusLabel.setText("Error performing search");
                }
            }
        };
        
        worker.execute();
    }
    
    private void displayResults(ArrayList<JSONObject> products) {
        resultsPanel.removeAll();
        
        if (products.isEmpty()) {
            JLabel noResultsLabel = new JLabel("No products found matching your criteria");
            noResultsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            noResultsLabel.setBorder(new EmptyBorder(20, 0, 0, 0));
            resultsPanel.add(noResultsLabel);
        } else {
            for (JSONObject product : products) {
                resultsPanel.add(createProductPanel(product));
            }
        }
        
        // Update UI
        resultsPanel.revalidate();
        resultsPanel.repaint();
        scrollPane.getVerticalScrollBar().setValue(0);
    }
    
    private JPanel createProductPanel(JSONObject product) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                new EmptyBorder(10, 10, 10, 10)));
        
        // Product name
        String name = product.optString("name", "Unknown Product");
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        // Product details (center)
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        
        // Category and brand
        String category = product.optString("category", "");
        String brand = product.optString("brand", "");
        JLabel categoryBrandLabel = new JLabel();
        
        if (!category.isEmpty() && !brand.isEmpty()) {
            categoryBrandLabel.setText(category + " | " + brand);
        } else if (!category.isEmpty()) {
            categoryBrandLabel.setText(category);
        } else if (!brand.isEmpty()) {
            categoryBrandLabel.setText(brand);
        }
        
        // Description
        String description = product.optString("description", "");
        JTextArea descriptionArea = new JTextArea(description);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setOpaque(false);
        descriptionArea.setEditable(false);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 12));
        descriptionArea.setRows(2);
        
        detailsPanel.add(categoryBrandLabel);
        detailsPanel.add(Box.createVerticalStrut(5));
        detailsPanel.add(descriptionArea);
        
        // Price and rating (right side)
        JPanel priceRatingPanel = new JPanel();
        priceRatingPanel.setLayout(new BoxLayout(priceRatingPanel, BoxLayout.Y_AXIS));
        priceRatingPanel.setPreferredSize(new Dimension(100, 80));
        
        double price = product.optDouble("price", 0.0);
        JLabel priceLabel = new JLabel("$" + String.format("%.2f", price));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        double rating = product.optDouble("rating", 0.0);
        JLabel ratingLabel = new JLabel(createStarRating(rating));
        ratingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        int stock = product.optInt("stock", 0);
        String stockText = stock > 0 ? "In Stock (" + stock + ")" : "Out of Stock";
        JLabel stockLabel = new JLabel(stockText);
        stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        stockLabel.setForeground(stock > 0 ? new Color(0, 128, 0) : Color.RED);
        
        priceRatingPanel.add(priceLabel);
        priceRatingPanel.add(Box.createVerticalStrut(5));
        priceRatingPanel.add(ratingLabel);
        priceRatingPanel.add(Box.createVerticalStrut(5));
        priceRatingPanel.add(stockLabel);
        
        panel.add(nameLabel, BorderLayout.NORTH);
        panel.add(detailsPanel, BorderLayout.CENTER);
        panel.add(priceRatingPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private String createStarRating(double rating) {
        StringBuilder stars = new StringBuilder();
        
        // Add filled stars
        int filledStars = (int) Math.floor(rating);
        for (int i = 0; i < filledStars; i++) {
            stars.append("★");
        }
        
        // Add half star if needed
        if (rating - filledStars >= 0.5) {
            stars.append("½");
            filledStars++;
        }
        
        // Add empty stars
        for (int i = filledStars; i < 5; i++) {
            stars.append("☆");
        }
        
        return stars + " (" + String.format("%.1f", rating) + ")";
    }
    
    private void resetFilters() {
        categoryComboBox.setSelectedItem("All Categories");
        brandComboBox.setSelectedItem("All Brands");
        priceSlider.setValue(0);
        ratingSlider.setValue(0);
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            ShoppingAssistantGUI gui = new ShoppingAssistantGUI();
            gui.setVisible(true);
        });
    }
}