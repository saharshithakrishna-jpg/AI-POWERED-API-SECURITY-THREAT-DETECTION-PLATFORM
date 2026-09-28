import React, { useState, useRef, useEffect } from 'react';
import { Terminal, Play, Loader, Trash2 } from 'lucide-react';
import api from '../services/api';
import './Simulator.css';

const Simulator = () => {
  const [loading, setLoading] = useState(false);
  const [logs, setLogs] = useState([]);
  const terminalRef = useRef(null);

  // Auto-scroll to bottom when new logs appear
  useEffect(() => {
    if (terminalRef.current) {
      terminalRef.current.scrollTop = terminalRef.current.scrollHeight;
    }
  }, [logs]);

  const addLog = (message, type = 'info') => {
    setLogs(prev => [...prev, { time: new Date().toLocaleTimeString(), message, type }]);
  };

  const clearLogs = () => setLogs([]);

  const simulateTraffic = async (type) => {
    if (loading) return;
    setLoading(true);
    setLogs([]);
    addLog(`Starting simulation: ${type}`);

    try {
      if (type === 'NORMAL') {
        // Uses authenticated endpoints to generate legitimate traffic
        const endpoints = [
          { method: 'get', url: '/api/dashboard/summary', label: 'GET /api/dashboard/summary' },
          { method: 'get', url: '/api/threats', label: 'GET /api/threats' },
          { method: 'get', url: '/api/dashboard/summary', label: 'GET /api/dashboard/summary' },
        ];
        for (const ep of endpoints) {
          try {
            await api[ep.method](ep.url, { skipAuthRedirect: true });
            addLog(`${ep.label} - 200 OK`, 'success');
          } catch (e) {
            const status = e.response?.status || 'ERR';
            addLog(`${ep.label} - ${status}`, status === 200 ? 'success' : 'warning');
          }
          await sleep(300);
        }
      } else if (type === 'BRUTE_FORCE') {
        // Simulate rapid failed login attempts (intentionally wrong password)
        for (let i = 0; i < 8; i++) {
          try {
            await api.post('/api/auth/login', { username: 'admin', password: 'wrongpassword123' }, { skipAuthRedirect: true });
            addLog(`POST /api/auth/login - 200 OK (unexpected)`, 'warning');
          } catch (e) {
            const status = e.response?.status || 'ERR';
            addLog(`POST /api/auth/login - ${status} Unauthorized`, 'error');
          }
          await sleep(150);
        }
      } else if (type === 'ENUMERATION') {
        // Try to probe hidden/internal endpoints
        const endpoints = [
          '/api/admin',
          '/api/internal/users',
          '/api/actuator/env',
          '/api/config',
          '/api/internal/debug',
        ];
        for (const ep of endpoints) {
          try {
            await api.get(ep, { skipAuthRedirect: true });
            addLog(`GET ${ep} - 200 OK`, 'success');
          } catch (e) {
            const status = e.response?.status || 'ERR';
            addLog(`GET ${ep} - ${status} ${getStatusText(status)}`, 'warning');
          }
          await sleep(200);
        }
      }

      addLog(`Simulation complete. Check the Threats page for detections.`, 'success');
    } catch (error) {
      addLog(`Unexpected error during simulation: ${error.message}`, 'error');
    } finally {
      setLoading(false);
    }
  };

  const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

  const getStatusText = (status) => {
    const texts = { 400: 'Bad Request', 401: 'Unauthorized', 403: 'Forbidden', 404: 'Not Found', 500: 'Server Error' };
    return texts[status] || '';
  };

  return (
    <div className="simulator-container">
      <h2 className="page-title">Traffic Simulator</h2>
      <p className="page-subtitle">Safely generate local traffic to test the detection engines.</p>

      <div className="simulator-layout">
        <div className="simulator-controls">
          <div className="control-card">
            <h3>Normal Traffic</h3>
            <p>Generates standard authenticated API requests.</p>
            <button
              className="btn btn-primary"
              onClick={() => simulateTraffic('NORMAL')}
              disabled={loading}
            >
              <Play size={16} /> Run Normal Traffic
            </button>
          </div>

          <div className="control-card">
            <h3>Brute Force Attack</h3>
            <p>Simulates rapid failed login attempts.</p>
            <button
              className="btn btn-danger"
              onClick={() => simulateTraffic('BRUTE_FORCE')}
              disabled={loading}
            >
              <Play size={16} /> Run Brute Force
            </button>
          </div>

          <div className="control-card">
            <h3>Endpoint Enumeration</h3>
            <p>Attempts to access hidden/internal endpoints.</p>
            <button
              className="btn btn-warning"
              onClick={() => simulateTraffic('ENUMERATION')}
              disabled={loading}
            >
              <Play size={16} /> Run Enumeration
            </button>
          </div>
        </div>

        <div className="terminal-window">
          <div className="terminal-header">
            <Terminal size={16} /> Simulation Output
            {loading && <Loader className="spin" size={16} style={{ marginLeft: 'auto' }} />}
            {!loading && logs.length > 0 && (
              <button
                className="clear-btn"
                onClick={clearLogs}
                title="Clear logs"
                style={{ marginLeft: 'auto', background: 'none', border: 'none', cursor: 'pointer', color: '#888' }}
              >
                <Trash2 size={14} />
              </button>
            )}
          </div>
          <div className="terminal-body" ref={terminalRef}>
            {logs.length === 0 && (
              <div className="text-gray">Click a button above to start a simulation...</div>
            )}
            {logs.map((log, index) => (
              <div key={index} className={`log-line log-${log.type}`}>
                <span className="log-time">[{log.time}]</span> {log.message}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Simulator;
