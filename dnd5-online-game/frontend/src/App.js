import React, { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Outlet } from 'react-router-dom';
import './App.css';
import Header from './components/header';
import ProtectedRoute from './components/ProtectedRoute';
import ErrorBoundary from './components/ErrorBoundary';
import useThemeStore from './stores/themeStore';
import HomePage from './pages/homePage';
import LoginPage from './pages/LoginPage';
import ProfilePage from './pages/ProfilePage';
import SessionPage from './pages/SessionPage';
import RegisterPage from './pages/RegisterPage';
import RoomsListPage from './pages/RoomsListPage';
import CharactersListPage from './pages/CharactersListPage';

const LayoutWithHeader = () => {
  const { theme, toggleTheme } = useThemeStore();

  return (
    <>
      <Header theme={theme} toggleTheme={toggleTheme} />
      <main className="main-container">
        <ErrorBoundary>
          <Outlet />
        </ErrorBoundary>
      </main>
    </>
  );
};

function App() {
  const theme = useThemeStore((state) => state.theme);

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  return (
    <div className="app-wrapper" data-theme={theme}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route element={<ProtectedRoute />}>
            <Route element={<LayoutWithHeader />}>
              <Route path="/" element={<HomePage />} />
              <Route path="/rooms" element={<RoomsListPage />} />
              <Route path="/characters" element={<CharactersListPage />} />
              <Route path="/profile" element={<ProfilePage />} />
            </Route>
            <Route path="/session/:roomId" element={
              <ErrorBoundary><SessionPage /></ErrorBoundary>
            } />
          </Route>
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
