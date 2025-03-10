import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

# Load dataset
df = pd.read_csv(r"E:\Engg\2nd year\4th sem\Labs\OOP Using Java\CP\backend\data\products.csv")
print(df.head())

# Create TF-IDF Vectorizer
vectorizer = TfidfVectorizer(stop_words="english")
tfidf_matrix = vectorizer.fit_transform(df['title'])

def recommend_products(query):
    query_vector = vectorizer.transform([query])
    similarities = cosine_similarity(query_vector, tfidf_matrix)
    
    # Get top 3 recommendations
    indices = similarities.argsort()[0][-3:][::-1]
    recommendations = df.iloc[indices][['title', 'category', 'price', 'image']].to_dict(orient='records')
    return recommendations
