import React from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { Shield, Activity, AlertTriangle, LogOut, Terminal } from 'lucide-react';
import './Layout.css';

const Layout = ({ onLogout }) => {
  const navigate = useNavigate();
  const username = localStorage.getItem('username');
  const role = localStorage.getItem('role');

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('username');
    if (onLogout) onLogout(); // Notify App.jsx to update auth state
    navigate('/login', { replace: true });
  };

  return (
    <div className="layout-container">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-header">
          <Shield className="brand-icon" />
          <h2>API Sentinel</h2>
        </div>

        <nav className="sidebar-nav">
          <NavLink to="/" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'} end>
            <Activity size={20} />
            <span>Dashboard</span>
          </NavLink>
          <NavLink to="/threats" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
            <AlertTriangle size={20} />
            <span>Threats</span>
          </NavLink>
          <NavLink to="/simulator" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
            <Terminal size={20} />
            <span>Traffic Simulator</span>
          </NavLink>
        </nav>

        <div className="sidebar-footer">
          <div className="user-info">
            <div className="user-avatar">{username ? username.charAt(0).toUpperCase() : 'U'}</div>
            <div className="user-details">
              <span className="user-name">{username}</span>
              <span className="user-role">{role}</span>
            </div>
          </div>
          <button onClick={handleLogout} className="logout-button">
            <LogOut size={18} />
            <span>Logout</span>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        <header className="topbar">
          <h1>API Security &amp; Threat Detection Platform</h1>
        </header>
        <div className="page-content">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default Layout;
