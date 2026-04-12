// Страница входа: логин + пароль, валидация, toggle показа пароля.
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { Shield } from 'lucide-react';
import useAuthStore from '../stores/authStore';
import useThemeStore from '../stores/themeStore';
import { loginRequest, getProfile } from '../api/auth';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';

const schema = yup.object({
  login: yup
    .string()
    .required('Введите логин')
    .min(3, 'Минимум 3 символа')
    .max(20, 'Максимум 20 символов')
    .matches(/^[a-zA-Z0-9а-яА-ЯёЁ]+$/, 'Только буквы и цифры'),
  password: yup
    .string()
    .required('Введите пароль')
    .min(6, 'Минимум 6 символов')
    .max(30, 'Максимум 30 символов'),
});

const LoginPage = () => {
  const navigate = useNavigate();
  const login = useAuthStore((state) => state.login);
  const [serverError, setServerError] = useState('');
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm({
    resolver: yupResolver(schema),
    mode: 'onChange',
  });

  const onSubmit = async (data) => {
    setServerError('');
    setLoading(true);
    try {
      const response = await loginRequest(data.login, data.password);
      login(response.data.token, { username: data.login });
      navigate('/');
    } catch (err) {
      const message =
        err.response?.data?.message || 'Неверный логин или пароль';
      setServerError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-logo">
          <Shield size={48} />
          <h1 className="login-title">
            DnD5 <span>Master</span>
          </h1>
        </div>
        <p className="login-subtitle">Войдите, чтобы начать приключение</p>

        <form onSubmit={handleSubmit(onSubmit)} className="login-form">
          <Input
            label="Логин"
            name="login"
            register={register}
            placeholder="Введите имя пользователя"
            error={errors.login?.message}
          />

          <Input
            label="Пароль"
            name="password"
            type="password"
            register={register}
            placeholder="Введите пароль"
            error={errors.password?.message}
          />

          {serverError && (
            <div className="server-error">{serverError}</div>
          )}

          <Button
            type="submit"
            variant="primary"
            disabled={!isValid || loading}
          >
            {loading ? 'Вход...' : 'Войти'}
          </Button>

          <p style={{ textAlign: 'center', marginTop: '0.5rem', fontSize: '0.9rem' }}>
            Нет аккаунта?{' '}
            <Link to="/register" style={{ color: 'var(--accent-red)', fontWeight: 600 }}>
              Зарегистрироваться
            </Link>
          </p>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;
