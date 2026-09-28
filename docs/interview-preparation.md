# Interview Preparation Guide

### Project explanation — 30 seconds
"For my final-year project, I built an AI-Powered API Security Platform. It's a full-stack application using Spring Boot and React that intercepts and logs API traffic, applying both hard-coded security rules and a machine-learning anomaly detection model to identify threats like brute-force attacks and rate-limit abuse in real-time."

### Project explanation — 2 minutes
"The API Security Platform is a comprehensive system designed to protect backend services. 
I built the core API using Java and Spring Boot. To monitor traffic, I implemented a custom Spring Security filter that intercepts every incoming request, measures its response time, and logs the metadata to a MySQL database asynchronously. 
The system evaluates this traffic against static rules—for example, if an IP fails authentication 5 times in 5 minutes, it's flagged as a Brute Force attack. 
However, since attackers often bypass static rules, I also built a Python microservice using FastAPI and scikit-learn. The Spring Boot backend sends traffic aggregates to this service, where an Isolation Forest model predicts if the behavior is anomalous.
Finally, I built a React dashboard for security analysts to review these threats, check the calculated risk scores, and manage incident resolution."

### Why Spring Boot?
Spring Boot provides robust enterprise-grade features out of the box. Its dependency injection, Spring Security integration for JWT, and Spring Data JPA made it the perfect choice to rapidly build a secure and database-driven backend.

### Why MySQL?
I chose a relational database because the data (Users, API Requests, Threats, Investigation Notes) is highly structured and requires clear relationships and ACID compliance. 

### Why Isolation Forest?
Cyberattacks are rare compared to normal traffic, making traditional classification difficult. Isolation Forest is an unsupervised anomaly detection algorithm that works by isolating outliers in the dataset, making it ideal for detecting previously unseen attack patterns based on request metadata.

### How is the risk score calculated?
The risk score (0-100) is calculated by taking a base score assigned to the threat type (e.g., Brute Force = 70) and adding dynamic contributions based on the frequency of the attack and the confidence score returned by the ML model.

### 5 Technical Interview Questions & Answers
1. **How do you prevent SQL Injection in your project?**
   *Answer*: By using Spring Data JPA and Hibernate, all database queries are automatically parameterized, preventing malicious SQL strings from altering the query logic.
2. **How does JWT authentication work in your app?**
   *Answer*: Upon valid login, the server signs a JSON object containing the user's username and role using a secret key. The React frontend stores this token and sends it in the `Authorization` header of subsequent requests. A Spring filter validates the signature before allowing access.
3. **What happens if the Python ML service goes down?**
   *Answer*: The Spring Boot service wraps the HTTP call in a try-catch block. If the ML service is unavailable, it catches the exception and returns a fallback response indicating no anomaly, ensuring the main API remains functional (graceful degradation).
4. **Why did you use a Filter instead of an Interceptor for monitoring?**
   *Answer*: Filters operate at the servlet level before request routing, allowing me to catch all requests, including those that might fail authentication at the Spring Security level.
5. **How did you handle CORS?**
   *Answer*: I configured a `CorsConfigurationSource` bean in Spring Security to explicitly allow requests from the React frontend's development port (`localhost:5173`) and allowed specific headers like `Authorization`.
