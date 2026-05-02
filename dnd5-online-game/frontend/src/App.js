import React, { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Outlet } from 'react-router-dom';
import './App.css';
import Header from './components/header';
import ProtectedRoute from './components/ProtectedRoute';
import ErrorBoundary from './components/ErrorBoundary';
import ToastContainer from './components/ui/ToastContainer';
import useThemeStore from './stores/themeStore';
import useToastStore from './stores/toastStore';
import { TZ_MESSAGES } from './utils/errorMessages';
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

  useEffect(() => {
    const pushToast = useToastStore.getState().push;
    let offlineToastId = null;

    const onOffline = () => {
      if (offlineToastId == null) {
        offlineToastId = pushToast(TZ_MESSAGES.NETWORK_OFFLINE, 'error', 0);
      }
      document.documentElement.setAttribute('data-offline', 'true');
    };
    const onOnline = () => {
      document.documentElement.removeAttribute('data-offline');
      if (offlineToastId != null) {
        useToastStore.getState().remove(offlineToastId);
        offlineToastId = null;
      }
      pushToast('Соединение восстановлено', 'success', 3000);
    };

    window.addEventListener('offline', onOffline);
    window.addEventListener('online', onOnline);
    if (!navigator.onLine) onOffline();

    return () => {
      window.removeEventListener('offline', onOffline);
      window.removeEventListener('online', onOnline);
    };
  }, []);

  return (
    <div className="app-wrapper" data-theme={theme}>
      <ToastContainer />
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
