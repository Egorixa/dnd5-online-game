import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { Shield } from 'lucide-react';
import { registerRequest } from '../api/auth';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import { parseApiError } from '../utils/apiError';
import { TZ_MESSAGES } from '../utils/errorMessages';
import useToastStore from '../stores/toastStore';

const schema = yup.object({
  username: yup
    .string()
    .required('Введите имя пользователя')
    .min(3, 'Минимум 3 символа')
    .max(20, 'Максимум 20 символов')
    .matches(/^[a-zA-Z0-9_]+$/, 'Только латиница, цифры и _'),
  password: yup
    .string()
    .required('Введите пароль')
    .min(6, 'Минимум 6 символов')
    .max(30, 'Максимум 30 символов'),
  confirmPassword: yup
    .string()
    .required('Подтвердите пароль')
    .oneOf([yup.ref('password')], 'Пароли не совпадают'),
});

const RegisterPage = () => {
  const navigate = useNavigate();
  const [serverError, setServerError] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

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
      await registerRequest(data.username, data.password, data.confirmPassword);
      setSuccess(true);
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      const parsed = parseApiError(err, 'Ошибка регистрации');
      const status = err?.response?.status;
      let message = parsed.message;
      if (parsed.code === 'USERNAME_TAKEN') {
        message = TZ_MESSAGES.USERNAME_TAKEN;
      } else if (status >= 500) {
        message = TZ_MESSAGES.SERVER_ERROR;
        useToastStore.getState().error(message);
      } else if (!err?.response) {
        message = TZ_MESSAGES.NETWORK_OFFLINE;
        useToastStore.getState().error(message);
      }
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
        <p className="login-subtitle">Создайте аккаунт для приключений</p>

        {success ? (
          <div style={{ textAlign: 'center' }}>
            <p style={{ color: 'var(--success-color)', fontWeight: 600, marginBottom: '0.5rem' }}>
              Регистрация успешна!
            </p>
            <p style={{ color: 'var(--text-secondary)' }}>
              Перенаправление на страницу входа...
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit(onSubmit)} className="login-form">
            <Input
              label="Имя пользователя"
              name="username"
              register={register}
              placeholder="Введите имя пользователя"
              error={errors.username?.message}
            />

            <Input
              label="Пароль"
              name="password"
              type="password"
              register={register}
              placeholder="Введите пароль"
              error={errors.password?.message}
            />

            <Input
              label="Подтверждение пароля"
              name="confirmPassword"
              type="password"
              register={register}
              placeholder="Повторите пароль"
              error={errors.confirmPassword?.message}
            />

            {serverError && (
              <div className="server-error">{serverError}</div>
            )}

            <Button
              type="submit"
              variant="primary"
              disabled={!isValid || loading}
            >
              {loading ? 'Регистрация...' : 'Зарегистрироваться'}
            </Button>

            <p style={{ textAlign: 'center', marginTop: '0.5rem', fontSize: '0.9rem' }}>
              Уже есть аккаунт?{' '}
              <Link to="/login" style={{ color: 'var(--accent-red)', fontWeight: 600 }}>
                Войти
              </Link>
            </p>
          </form>
        )}
      </div>
    </div>
  );
};

export default RegisterPage;
