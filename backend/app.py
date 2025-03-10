import pandas as pd
import json
import re
from flask import Flask, request, jsonify

app = Flask(__name__)

# Load the product dataset
def load_product_data():
    try:
        df = pd.read_csv('products.csv', dtype={'id': str, 'name': str, 'category': str, 'brand': str, 'description': str})
        
        # Ensure numeric columns are properly converted
        df['price'] = pd.to_numeric(df['price'], errors='coerce')
        df['rating'] = pd.to_numeric(df['rating'], errors='coerce')
        df['stock'] = pd.to_numeric(df['stock'], errors='coerce').astype('Int64')  # Allows NaN

        return df
    except Exception as e:
        print(f"Error loading data: {e}")
        return pd.DataFrame()

products_df = load_product_data()

# Search function
def search_products(query, filters=None):
    if products_df.empty:
        return []

    query = query.lower()
    mask = pd.Series(False, index=products_df.index)

    for col in ['name', 'category', 'brand', 'description']:
        mask |= products_df[col].astype(str).str.lower().str.contains(query, na=False)

    # Apply filters
    if filters:
        if 'category' in filters and filters['category']:
            mask &= products_df['category'].str.lower() == filters['category'].lower()
        if 'brand' in filters and filters['brand']:
            mask &= products_df['brand'].str.lower() == filters['brand'].lower()
        if 'min_price' in filters and filters['min_price'] is not None:
            mask &= products_df['price'] >= float(filters['min_price'])
        if 'max_price' in filters and filters['max_price'] is not None:
            mask &= products_df['price'] <= float(filters['max_price'])
        if 'min_rating' in filters and filters['min_rating'] is not None:
            mask &= products_df['rating'] >= float(filters['min_rating'])

    filtered_products = products_df[mask].sort_values(by='rating', ascending=False)
    return filtered_products.head(10).to_dict(orient='records')

# API Endpoints
@app.route('/api/query', methods=['GET'])
def handle_query():
    user_query = request.args.get('query', '')

    if not user_query:
        return jsonify({"error": "No query provided"}), 400

    print(f"Received query: {user_query}")

    filters = {}
    products = search_products(user_query, filters)

    response = {
        "query": user_query,
        "products": products,
        "message": "Search results for your query."
    }
    return jsonify(response)

@app.route('/api/products', methods=['GET'])
def get_all_products():
    if products_df.empty:
        return jsonify({"error": "No products available"}), 500

    return jsonify(products_df.head(50).to_dict(orient='records'))

# Start Flask server
if __name__ == '__main__':
    if products_df.empty:
        print("Error: Could not load product data.")
    else:
        print(f"Loaded {len(products_df)} products successfully.")
        app.run(debug=True, port=5000)
