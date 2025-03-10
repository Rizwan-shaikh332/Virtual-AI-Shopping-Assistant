import pandas as pd

def load_product_data():
    try:
        df = pd.read_csv('products.csv', dtype={'id': str})  # Read CSV

        # Convert numeric columns safely
        df['price'] = pd.to_numeric(df['price'], errors='coerce')
        df['rating'] = pd.to_numeric(df['rating'], errors='coerce')
        df['stock'] = pd.to_numeric(df['stock'], errors='coerce').astype('Int64')

        # Convert all text columns to strings, handling NaN values
        text_columns = ['name', 'category', 'brand', 'description']
        for col in text_columns:
            if col in df.columns:
                df[col] = df[col].astype(str).fillna('').str.lower()

        return df
    except Exception as e:
        print(f"Error loading data: {e}")
        return pd.DataFrame()

# 🔹 Test the CSV loading function
df = load_product_data()

# 🔹 Print first few rows to verify
print("\n📌 First 5 rows of the dataset:")
print(df.head())

# 🔹 Print column data types
print("\n📌 Column Data Types:")
print(df.dtypes)

# 🔹 Check for non-string values in text columns
print("\n📌 Column Type Check:")
for col in ['name', 'category', 'brand', 'description']:
    if col in df.columns:
        non_string_values = df[col].apply(lambda x: type(x)).unique()
        print(f"Column '{col}' contains types: {non_string_values}")
