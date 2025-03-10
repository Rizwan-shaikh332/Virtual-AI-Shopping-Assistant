public class Product {
    private String id;
    private String name;
    private String category;
    private String brand;
    private String description;
    private double price;
    private double rating;
    private int stock;
    
    public Product(String id, String name, String category, String brand, 
                  String description, double price, double rating, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.description = description;
        this.price = price;
        this.rating = rating;
        this.stock = stock;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getBrand() { return brand; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public double getRating() { return rating; }
    public int getStock() { return stock; }
    
    // Formatted price as string
    public String getFormattedPrice() {
        return String.format("$%.2f", price);
    }
    
    // Formatted rating with stars
    public String getFormattedRating() {
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
    
    // Check if product is in stock
    public boolean isInStock() {
        return stock > 0;
    }
    
    // Get stock status as string
    public String getStockStatus() {
        return isInStock() ? "In Stock (" + stock + ")" : "Out of Stock";
    }
    
    @Override
    public String toString() {
        return name + " - " + getFormattedPrice();
    }
}