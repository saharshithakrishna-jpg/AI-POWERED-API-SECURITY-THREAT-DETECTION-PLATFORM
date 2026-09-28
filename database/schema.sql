-- Create Database
CREATE DATABASE IF NOT EXISTS api_security_db;
USE api_security_db;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- ADMIN, ANALYST, USER
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- API Requests Table (for monitoring)
CREATE TABLE IF NOT EXISTS api_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    source_ip VARCHAR(50) NOT NULL,
    user_id BIGINT,
    response_status INT NOT NULL,
    response_time INT NOT NULL,
    user_agent VARCHAR(255),
    request_size INT,
    response_size INT,
    authenticated BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes for API requests to speed up rule evaluation
CREATE INDEX idx_source_ip_timestamp ON api_requests(source_ip, timestamp);
CREATE INDEX idx_endpoint_timestamp ON api_requests(endpoint, timestamp);

-- Threats Table
CREATE TABLE IF NOT EXISTS threats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    threat_type VARCHAR(50) NOT NULL, -- BRUTE_FORCE, RATE_LIMIT_ABUSE, etc.
    severity VARCHAR(20) NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
    risk_score INT NOT NULL, -- 0 to 100
    detection_method VARCHAR(50) NOT NULL, -- RULE_BASED, ML_BASED, HYBRID
    source_ip VARCHAR(50) NOT NULL,
    endpoint VARCHAR(255),
    http_method VARCHAR(10),
    timestamp TIMESTAMP NOT NULL,
    request_count INT,
    authentication_failures INT,
    ml_anomaly_score DOUBLE,
    description TEXT,
    recommended_action TEXT,
    status VARCHAR(30) DEFAULT 'OPEN', -- OPEN, INVESTIGATING, RESOLVED, FALSE_POSITIVE
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Threat Events (linking threats to specific API requests)
CREATE TABLE IF NOT EXISTS threat_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    threat_id BIGINT NOT NULL,
    api_request_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (threat_id) REFERENCES threats(id) ON DELETE CASCADE,
    FOREIGN KEY (api_request_id) REFERENCES api_requests(id) ON DELETE CASCADE
);

-- Investigation Notes Table
CREATE TABLE IF NOT EXISTS investigation_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    threat_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    note TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (threat_id) REFERENCES threats(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Insert sample admin user (password: admin123, hash generated using BCrypt)
-- We'll insert this via Spring Boot DataInitialization later, or leave it here.
-- INSERT INTO users (username, password, role) VALUES ('admin', '$2a$10$wK/...', 'ADMIN');
