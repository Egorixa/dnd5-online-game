import React, { useState } from 'react';
import './App.css';
import Header from './components/header';
import HomePage from './pages/homePage';

function App() {
    const [theme, setTheme] = useState('light');

    const toggleTheme = () => {
        setTheme(prev => prev === 'light' ? 'dark' : 'light');
    };

    return (
        <div className="app-wrapper" data-theme={theme}>
            <Header theme={theme} toggleTheme={toggleTheme} />
            <main className="main-container">
                <HomePage />
            </main>
        </div>
    );
}

export default App;