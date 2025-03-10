import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class ApiClient {
    private final String baseUrl;
    
    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public JSONObject searchProducts(String query, String category, String brand, 
                                    Double minPrice, Double maxPrice, Double minRating) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(baseUrl + "/query?query=" + URLEncoder.encode(query, "UTF-8"));
        
        // Add category filter
        if (category != null && !category.isEmpty() && !category.equals("All Categories")) {
            urlBuilder.append("&category=").append(URLEncoder.encode(category, "UTF-8"));
        }
        
        // Add brand filter
        if (brand != null && !brand.isEmpty() && !brand.equals("All Brands")) {
            urlBuilder.append("&brand=").append(URLEncoder.encode(brand, "UTF-8"));
        }
        
        // Add price filters
        if (minPrice != null) {
            urlBuilder.append("&min_price=").append(minPrice);
        }
        if (maxPrice != null) {
            urlBuilder.append("&max_price=").append(maxPrice);
        }
        
        // Add rating filter
        if (minRating != null) {
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
            
            return new JSONObject(response.toString());
        } else {
            throw new Exception("API request failed with response code: " + responseCode);
        }
    }
    
    public ArrayList<JSONObject> getAllProducts() throws Exception {
        URL url = new URL(baseUrl + "/products");
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
            JSONArray productsArray = new JSONArray(response.toString());
            ArrayList<JSONObject> products = new ArrayList<>();
            
            for (int i = 0; i < productsArray.length(); i++) {
                products.add(productsArray.getJSONObject(i));
            }
            
            return products;
        } else {
            throw new Exception("API request failed with response code: " + responseCode);
        }
    }
    
    public ArrayList<String> getUniqueCategories() throws Exception {
        ArrayList<JSONObject> products = getAllProducts();
        ArrayList<String> categories = new ArrayList<>();
        
        for (JSONObject product : products) {
            if (product.has("category") && !product.isNull("category")) {
                String category = product.getString("category");
                if (!categories.contains(category)) {
                    categories.add(category);
                }
            }
        }
        
        return categories;
    }
    
    public ArrayList<String> getUniqueBrands() throws Exception {
        ArrayList<JSONObject> products = getAllProducts();
        ArrayList<String> brands = new ArrayList<>();
        
        for (JSONObject product : products) {
            if (product.has("brand") && !product.isNull("brand")) {
                String brand = product.getString("brand");
                if (!brands.contains(brand)) {
                    brands.add(brand);
                }
            }
        }
        
        return brands;
    }
}