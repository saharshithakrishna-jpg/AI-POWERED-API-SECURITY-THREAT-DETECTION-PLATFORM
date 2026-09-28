import React, { useState, useEffect } from 'react';
import { Activity, ShieldAlert, AlertTriangle, ShieldCheck, RefreshCw } from 'lucide-react';
import api from '../services/api';
import './Dashboard.css';

const Dashboard = () => {
  const [summary, setSummary] = useState({
    totalRequests: 0,
    requestsToday: 0,
    detectedThreats: 0,
    openIncidents: 0
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchSummary = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.get('/api/dashboard/summary');
      setSummary(response.data);
    } catch (err) {
      console.error('Failed to fetch dashboard summary', err);
      if (err.code === 'ERR_NETWORK' || err.code === 'ECONNREFUSED') {
        setError('Backend server is offline. Start the Spring Boot server on port 8080.');
      } else if (err.response?.status === 403) {
        setError('Access denied. Please log out and log in again.');
      } else {
        setError('Failed to load dashboard data. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSummary();
  }, []);

  return (
    <div className="dashboard-container">
      <div className="header-actions">
        <h2 className="page-title">Overview</h2>
        <button className="btn btn-secondary" onClick={fetchSummary} disabled={loading}>
          <RefreshCw size={14} /> Refresh
        </button>
      </div>

      {error && (
        <div className="error-banner">
          ⚠️ {error}
        </div>
      )}

      {loading ? (
        <div className="loading">Loading dashboard data...</div>
      ) : (
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon-bg bg-blue">
              <Activity className="stat-icon text-blue" />
            </div>
            <div className="stat-content">
              <h3>Total API Requests</h3>
              <p className="stat-value">{(summary?.totalRequests ?? 0).toLocaleString()}</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon-bg bg-green">
              <ShieldCheck className="stat-icon text-green" />
            </div>
            <div className="stat-content">
              <h3>Requests Today</h3>
              <p className="stat-value">{(summary?.requestsToday ?? 0).toLocaleString()}</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon-bg bg-red">
              <ShieldAlert className="stat-icon text-red" />
            </div>
            <div className="stat-content">
              <h3>Detected Threats</h3>
              <p className="stat-value">{(summary?.detectedThreats ?? 0).toLocaleString()}</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon-bg bg-orange">
              <AlertTriangle className="stat-icon text-orange" />
            </div>
            <div className="stat-content">
              <h3>Open Incidents</h3>
              <p className="stat-value">{(summary?.openIncidents ?? 0).toLocaleString()}</p>
            </div>
          </div>
        </div>
      )}

      <div className="dashboard-content">
        <div className="panel">
          <h3>Welcome to API Sentinel</h3>
          <p>This academic project demonstrates a full-stack API security monitoring platform.</p>
          <ul>
            <li><strong>Rule-Based Engine:</strong> Detects brute-force, rate-limit abuse, and enumeration.</li>
            <li><strong>Machine Learning:</strong> Uses Isolation Forest for anomaly detection.</li>
            <li><strong>Risk Scoring:</strong> Normalizes threat severity based on multiple signals.</li>
          </ul>
          <div className="credentials-box">
            <strong>Default Login Credentials:</strong>
            <code>Username: admin &nbsp;|&nbsp; Password: admin123</code>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
