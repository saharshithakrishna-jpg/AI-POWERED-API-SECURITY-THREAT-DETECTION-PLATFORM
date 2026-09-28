from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import numpy as np
import pandas as pd
import joblib
import os

app = FastAPI(title="API Security ML Service")

class PredictionInput(BaseModel):
    requests_per_minute: float
    failed_requests: float
    unique_endpoints: float
    avg_response_time: float
    auth_failures: float

class PredictionOutput(BaseModel):
    is_anomaly: bool
    anomaly_score: float

# Try to load model on startup
model_path = os.path.join(os.path.dirname(__file__), "model", "isolation_forest.joblib")
model = None

@app.on_event("startup")
def load_model():
    global model
    try:
        if os.path.exists(model_path):
            model = joblib.load(model_path)
            print(f"Loaded ML model from {model_path}")
        else:
            print(f"Model not found at {model_path}. Please train it first.")
    except Exception as e:
        print(f"Error loading model: {e}")

@app.post("/predict", response_model=PredictionOutput)
def predict(data: PredictionInput):
    if model is None:
        # Fallback if model not trained yet, just for testing
        # We simulate anomaly if requests > 100 or auth_failures > 10
        is_anomaly = data.requests_per_minute > 100 or data.auth_failures > 10
        score = 0.9 if is_anomaly else 0.1
        return PredictionOutput(is_anomaly=is_anomaly, anomaly_score=score)

    try:
        # Prepare data for model
        input_data = pd.DataFrame([{
            "requests_per_minute": data.requests_per_minute,
            "failed_requests": data.failed_requests,
            "unique_endpoints": data.unique_endpoints,
            "avg_response_time": data.avg_response_time,
            "auth_failures": data.auth_failures
        }])

        # Predict (1 for normal, -1 for anomaly)
        prediction = model.predict(input_data)
        
        # decision_function returns negative values for anomalies and positive for normal
        # we negate it so higher score = higher anomaly likelihood
        raw_score = -model.decision_function(input_data)[0]
        
        # Normalize score to roughly 0-1 based on expected ranges
        # Sigmoid can work nicely
        anomaly_score = 1 / (1 + np.exp(-raw_score))

        is_anomaly = prediction[0] == -1

        return PredictionOutput(is_anomaly=bool(is_anomaly), anomaly_score=float(anomaly_score))

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
def health_check():
    return {"status": "ok", "model_loaded": model is not None}
