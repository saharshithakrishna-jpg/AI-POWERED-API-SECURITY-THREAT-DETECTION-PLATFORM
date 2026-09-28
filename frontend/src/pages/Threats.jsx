import React, { useState, useEffect } from 'react';
import { AlertCircle, CheckCircle, RefreshCw } from 'lucide-react';
import api from '../services/api';
import './Threats.css';

const Threats = () => {
  const [threats, setThreats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [updatingId, setUpdatingId] = useState(null);

  const fetchThreats = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.get('/api/threats');
      const list = Array.isArray(response.data)
        ? response.data
        : (Array.isArray(response.data?.content) ? response.data.content : []);
      setThreats(list);
    } catch (err) {
      console.error('Failed to fetch threats', err);
      if (err.code === 'ERR_NETWORK' || err.code === 'ECONNREFUSED' || !err.response) {
        setError('Cannot connect to backend service. Please ensure Spring Boot is running on port 8080.');
      } else {
        setError('Failed to fetch threats. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchThreats();
  }, []);

  const getSeverityBadge = (severity) => {
    const classes = {
      CRITICAL: 'badge-critical',
      HIGH: 'badge-high',
      MEDIUM: 'badge-medium',
      LOW: 'badge-low',
    };
    return <span className={`badge ${classes[severity] || 'badge-low'}`}>{severity}</span>;
  };

  const getStatusBadge = (status) => {
    const classes = {
      OPEN: 'status-open',
      INVESTIGATING: 'status-investigating',
      RESOLVED: 'status-resolved',
      FALSE_POSITIVE: 'status-false-positive',
    };
    return <span className={`status-badge ${classes[status] || ''}`}>{status}</span>;
  };

  const getRiskScoreClass = (score) => {
    if (!score) return '';
    if (score >= 70) return 'risk-high';
    if (score >= 40) return 'risk-mid';
    return 'risk-low';
  };

  const updateStatus = async (id, status) => {
    setUpdatingId(id);
    try {
      await api.patch(`/api/threats/${id}/status`, { status });
      await fetchThreats();
    } catch (err) {
      console.error('Failed to update status', err);
      alert('Failed to update status. Please check your backend connection.');
    } finally {
      setUpdatingId(null);
    }
  };

  return (
    <div className="threats-container">
      <div className="header-actions">
        <h2 className="page-title">Detected Threats</h2>
        <button className="btn btn-secondary" onClick={fetchThreats} disabled={loading}>
          <RefreshCw size={14} className={loading ? 'spin' : ''} /> Refresh
        </button>
      </div>

      {error && (
        <div className="error-banner">
          ⚠️ {error}
        </div>
      )}

      {loading ? (
        <div className="loading">Loading threats...</div>
      ) : threats.length === 0 ? (
        <div className="empty-state">
          <CheckCircle size={48} className="text-green mb-4" />
          <h3>All clear!</h3>
          <p>No security threats have been detected.</p>
        </div>
      ) : (
        <div className="table-container">
          <table className="threats-table">
            <thead>
              <tr>
                <th>Time</th>
                <th>Type</th>
                <th>Severity</th>
                <th>Risk Score</th>
                <th>IP Source</th>
                <th>Endpoint</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {threats.map((threat) => (
                <tr key={threat.id}>
                  <td>{new Date(threat.timestamp).toLocaleString()}</td>
                  <td>{threat.threatType?.replace(/_/g, ' ') || 'UNKNOWN'}</td>
                  <td>{getSeverityBadge(threat.severity)}</td>
                  <td>
                    <span className={`risk-score ${getRiskScoreClass(threat.riskScore)}`}>
                      {threat.riskScore ?? 'N/A'}
                    </span>
                  </td>
                  <td><code>{threat.sourceIp}</code></td>
                  <td>{threat.endpoint}</td>
                  <td>{getStatusBadge(threat.status)}</td>
                  <td>
                    {threat.status === 'OPEN' && (
                      <button
                        className="btn btn-sm btn-secondary"
                        onClick={() => updateStatus(threat.id, 'INVESTIGATING')}
                        disabled={updatingId === threat.id}
                      >
                        {updatingId === threat.id ? 'Updating...' : 'Investigate'}
                      </button>
                    )}
                    {threat.status === 'INVESTIGATING' && (
                      <div className="action-group">
                        <button
                          className="btn btn-sm btn-success"
                          onClick={() => updateStatus(threat.id, 'RESOLVED')}
                          disabled={updatingId === threat.id}
                        >
                          Resolve
                        </button>
                        <button
                          className="btn btn-sm btn-warning"
                          onClick={() => updateStatus(threat.id, 'FALSE_POSITIVE')}
                          disabled={updatingId === threat.id}
                        >
                          False Positive
                        </button>
                      </div>
                    )}
                    {threat.status !== 'OPEN' && threat.status !== 'INVESTIGATING' && (
                      <span style={{ color: '#64748b', fontSize: '0.8rem' }}>Closed</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default Threats;
