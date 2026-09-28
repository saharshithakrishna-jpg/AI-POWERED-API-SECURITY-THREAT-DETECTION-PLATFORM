# Machine Learning Model

## Methodology
The platform utilizes an **Anomaly Detection** approach rather than standard classification. This is because in a real-world scenario, cyberattacks constantly evolve, making it difficult to maintain a perfectly labeled dataset of every possible attack.

## Algorithm: Isolation Forest
We use `IsolationForest` from `scikit-learn`. 
- **Why?**: Isolation Forest explicitly isolates anomalies instead of profiling normal data points. It works well for high-dimensional data and is efficient for real-time predictions.

## Features Used
The model does not look at payloads (to maintain privacy and speed). It looks at behavioral metadata aggregated over a 5-minute rolling window for a specific IP:
1. `requests_per_minute`: Total volume.
2. `failed_requests`: Count of 4xx and 5xx responses.
3. `unique_endpoints`: Number of distinct API paths accessed (high = enumeration).
4. `avg_response_time`: Server processing time (high = possible DoS or heavy queries).
5. `auth_failures`: Count of 401/403 responses.

## Dataset
Because real-world attack data containing these specific aggregated metrics is rare and often confidential, the model is trained on a **Synthetic Dataset**.
- The script `train_model.py` generates thousands of records simulating both normal user traffic and anomalous traffic (brute force, scraping, DoS).
- **Limitation**: The model is only as good as the synthetic data. In a production environment, this model should be retrained on actual historical logs from the organization's API gateway.
