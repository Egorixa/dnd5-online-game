import React, { useState } from 'react';
import { useNavigate, NavLink } from 'react-router-dom';
import { Shield, User, LogOut, Settings, Sun, Moon, Home, PlusCircle, ListChecks, Users } from 'lucide-react';
import useAuthStore from '../stores/authStore';

const Header = ({ theme, toggleTheme }) => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const navigate = useNavigate();
  const logout = useAuthStore((state) => state.logout);

  const handleLogout = () => {
    logout();
    navigate('/login');
    setIsMenuOpen(false);
  };

  const handleProfile = () => {
    navigate('/profile');
    setIsMenuOpen(false);
  };

  return (
    <header className="app-header">
      <div className="header-content">
        <div className="logo-section" onClick={() => navigate('/')}>
          <div className="logo-icon">
            <Shield size={28} fill="currentColor" />
          </div>
          <span className="logo-text">DnD5 <span>Master</span></span>
        </div>

        <nav className="header-nav">
          <NavLink to="/" end className={({ isActive }) => `header-nav-link ${isActive ? 'active' : ''}`}>
            <PlusCircle size={16} />
            <span>Создать комнату</span>
          </NavLink>
          <NavLink to="/rooms" className={({ isActive }) => `header-nav-link ${isActive ? 'active' : ''}`}>
            <ListChecks size={16} />
            <span>Список комнат</span>
          </NavLink>
          <NavLink to="/characters" className={({ isActive }) => `header-nav-link ${isActive ? 'active' : ''}`}>
            <Users size={16} />
            <span>Персонажи</span>
          </NavLink>
        </nav>

        <div className="header-actions">
          <button className="icon-btn theme-toggle" onClick={toggleTheme}>
            {theme === 'light' ? <Moon size={20} /> : <Sun size={20} />}
          </button>

          <div className="profile-container">
            <button
              className={`profile-trigger ${isMenuOpen ? 'active' : ''}`}
              onClick={() => setIsMenuOpen(!isMenuOpen)}
            >
              <User size={22} />
            </button>

            {isMenuOpen && (
              <div className="profile-dropdown">
                <div className="dropdown-item" onClick={() => { navigate('/'); setIsMenuOpen(false); }}>
                  <Home size={16} />
                  <span>Главная</span>
                </div>
                <div className="dropdown-item" onClick={() => { navigate('/rooms'); setIsMenuOpen(false); }}>
                  <ListChecks size={16} />
                  <span>Список комнат</span>
                </div>
                <div className="dropdown-item" onClick={handleProfile}>
                  <Settings size={16} />
                  <span>Профиль</span>
                </div>
                <div className="dropdown-divider"></div>
                <div className="dropdown-item logout" onClick={handleLogout}>
                  <LogOut size={16} />
                  <span>Выход</span>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;
