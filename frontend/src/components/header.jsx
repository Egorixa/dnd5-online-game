import React, { useState } from 'react';
import { Shield, User, LogOut, Settings, Sun, Moon } from 'lucide-react';

const Header = ({ theme, toggleTheme }) => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    return (
        <header className="app-header">
            <div className="header-content">
                <div className="logo-section" onClick={() => window.location.href = '/'}>
                    <div className="logo-icon">
                        <Shield size={28} fill="currentColor" />
                    </div>
                    <span className="logo-text">DnD5 <span>Master</span> </span>
                </div>

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
                                <div className="dropdown-item">
                                    <Settings size={16} />
                                    <span>Профиль</span>
                                </div>
                                <div className="dropdown-divider"></div>
                                <div className="dropdown-item logout">
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