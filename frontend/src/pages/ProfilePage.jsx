// Профиль пользователя: статистика, переключатель темы, инструкция по созданию комнаты.
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { User, Calendar, Trophy, Swords, Crown, LogOut, Sun, Moon, Info } from 'lucide-react';
import useAuthStore from '../stores/authStore';
import useThemeStore from '../stores/themeStore';
import { getProfile } from '../api/auth';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';

const ProfilePage = () => {
  const navigate = useNavigate();
  const { user, logout, updateUser } = useAuthStore();
  const { theme, toggleTheme } = useThemeStore();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchProfile = async () => {
      setLoading(true);
      try {
        const response = await getProfile();
        updateUser(response.data);
      } catch {
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [updateUser]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const displayUser = user || {};

  return (
    <div className="profile-page">
      <h1 className="page-title">Профиль</h1>

      <div className="profile-grid">
        <Card className="profile-info-card">
          <div className="profile-avatar">
            <User size={64} />
          </div>
          <h2 className="profile-username">{displayUser.username || displayUser.login || 'Игрок'}</h2>

          <div className="profile-stats">
            <div className="stat-item">
              <Calendar size={20} />
              <div>
                <span className="stat-label">Дата регистрации</span>
                <span className="stat-value">
                  {displayUser.registrationDate
                    ? new Date(displayUser.registrationDate).toLocaleDateString('ru-RU')
                    : '—'}
                </span>
              </div>
            </div>

            <div className="stat-item">
              <Trophy size={20} />
              <div>
                <span className="stat-label">Победы</span>
                <span className="stat-value">{displayUser.wins ?? 0}</span>
              </div>
            </div>

            <div className="stat-item">
              <Swords size={20} />
              <div>
                <span className="stat-label">Поражения</span>
                <span className="stat-value">{displayUser.defeats ?? displayUser.losses ?? 0}</span>
              </div>
            </div>

            <div className="stat-item">
              <Crown size={20} />
              <div>
                <span className="stat-label">Партий как Мастер</span>
                <span className="stat-value">{displayUser.countMasterTime ?? displayUser.masterGames ?? 0}</span>
              </div>
            </div>
          </div>
        </Card>

        <Card className="profile-settings-card">
          <h3>Настройки</h3>

          <div className="setting-row">
            <span>Тема оформления</span>
            <button className="theme-switch" onClick={toggleTheme}>
              {theme === 'light' ? <Moon size={20} /> : <Sun size={20} />}
              <span>{theme === 'light' ? 'Темная' : 'Светлая'}</span>
            </button>
          </div>

          <div className="setting-row">
            <Button variant="danger" onClick={handleLogout}>
              <LogOut size={18} />
              Выйти из системы
            </Button>
          </div>
        </Card>
      </div>

      <Card className="profile-howto-card">
        <h3 className="profile-howto-title">
          <Info size={20} /> Как создать комнату и пригласить игроков
        </h3>
        <ol className="profile-howto-list">
          <li>
            На <strong>главной странице</strong> введите название будущей сессии и выберите
            тип доступа: <em>«Публичная»</em> — комната будет видна игрокам в поиске
            на Android-клиенте, <em>«Приватная»</em> — присоединиться можно только
            по коду приглашения.
          </li>
          <li>
            Нажмите кнопку <strong>«Создать новую комнату»</strong>. Вы попадёте
            на экран сессии, а сверху появится уникальный <strong>код приглашения</strong>.
          </li>
          <li>
            Скопируйте код кнопкой рядом с ним и передайте его игрокам — они вводят
            этот код в Android-приложении в разделе <em>«Игры → Присоединиться по коду»</em>.
          </li>
          <li>
            Когда игроки подключатся, они появятся в левом списке участников. Кликните
            по нужному игроку, чтобы открыть его лист персонажа в центре и управлять
            боем, инициативой, бросками кубиков и заклинаниями.
          </li>
          <li>
            После окончания партии нажмите <strong>«Завершить сессию»</strong> в правом
            верхнем углу — счётчик <em>«Партий как Мастер»</em> увеличится автоматически.
          </li>
        </ol>
      </Card>
    </div>
  );
};

export default ProfilePage;
