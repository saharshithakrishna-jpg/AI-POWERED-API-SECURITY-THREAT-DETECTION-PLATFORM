import pandas as pd
import numpy as np
from sklearn.ensemble import IsolationForest
import joblib
import os

# Generate synthetic dataset for API traffic
def generate_dataset(n_samples=5000):
    np.random.seed(42)
    
    # Normal traffic
    normal_samples = int(n_samples * 0.95)
    df_normal = pd.DataFrame({
        "requests_per_minute": np.random.normal(20, 10, normal_samples),
        "failed_requests": np.random.poisson(1, normal_samples),
        "unique_endpoints": np.random.normal(5, 2, normal_samples),
        "avg_response_time": np.random.normal(150, 50, normal_samples),
        "auth_failures": np.random.poisson(0.5, normal_samples)
    })
    
    # Anomalous traffic (Brute force, rate limit abuse, etc.)
    anomaly_samples = n_samples - normal_samples
    df_anomaly = pd.DataFrame({
        "requests_per_minute": np.random.normal(300, 100, anomaly_samples),
        "failed_requests": np.random.poisson(50, anomaly_samples),
        "unique_endpoints": np.random.normal(20, 5, anomaly_samples),
        "avg_response_time": np.random.normal(500, 200, anomaly_samples),
        "auth_failures": np.random.poisson(40, anomaly_samples)
    })
    
    # Combine and clean
    df = pd.concat([df_normal, df_anomaly])
    df = df.clip(lower=0) # No negative values
    
    return df

def train_model():
    print("Generating synthetic dataset...")
    df = generate_dataset()
    
    print("Training Isolation Forest model...")
    # contamination is the expected proportion of outliers
    model = IsolationForest(n_estimators=100, contamination=0.05, random_state=42)
    model.fit(df)
    
    # Save the model
    model_dir = os.path.join(os.path.dirname(__file__), "model")
    os.makedirs(model_dir, exist_ok=True)
    model_path = os.path.join(model_dir, "isolation_forest.joblib")
    
    joblib.dump(model, model_path)
    print(f"Model saved successfully to {model_path}")

if __name__ == "__main__":
    train_model()
