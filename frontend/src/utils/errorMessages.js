export const TZ_MESSAGES = {
  AUTH_INVALID: 'Неверное имя пользователя или пароль',
  USERNAME_TAKEN: 'Пользователь с таким именем уже существует',
  PASSWORDS_MISMATCH: 'Введённые пароли не совпадают',
  FIELD_REQUIRED: 'Поле обязательно для заполнения',
  FIELD_TOO_LONG: 'Превышена максимально допустимая длина',
  FIELD_NOT_NUMBER: 'Введите числовое значение',
  ROOM_NOT_FOUND: 'Комната с указанным кодом не найдена',
  ROOM_UNAVAILABLE: 'Подключение к комнате невозможно',
  NETWORK_OFFLINE: 'Нет соединения с сервером. Повторите попытку',
  NETWORK_TIMEOUT: 'Превышено время ожидания ответа',
  SERVER_ERROR: 'Внутренняя ошибка сервера. Попробуйте позже',
  SESSION_EXPIRED: 'Сеанс истёк. Войдите повторно',
};

export const mapErrorToTzMessage = (err) => {
  if (!err) return null;

  if (err.code === 'ERR_NETWORK' || err.message === 'Network Error') {
    return TZ_MESSAGES.NETWORK_OFFLINE;
  }
  if (err.code === 'ECONNABORTED' || /timeout/i.test(err.message || '')) {
    return TZ_MESSAGES.NETWORK_TIMEOUT;
  }

  const status = err.response?.status;
  const data = err.response?.data;
  const code = data?.code;

  if (status === 401) {
    if (data?.message?.toLowerCase().includes('invalid credentials') ||
        code === 'INVALID_CREDENTIALS' ||
        code === 'AUTH_INVALID') {
      return TZ_MESSAGES.AUTH_INVALID;
    }
    return TZ_MESSAGES.SESSION_EXPIRED;
  }
  if (code === 'USERNAME_TAKEN') return TZ_MESSAGES.USERNAME_TAKEN;
  if (status === 404) return TZ_MESSAGES.ROOM_NOT_FOUND;
  if (status === 403 || code === 'ALREADY_FINISHED' || code === 'ALREADY_JOINED') {
    return TZ_MESSAGES.ROOM_UNAVAILABLE;
  }
  if (status >= 500) return TZ_MESSAGES.SERVER_ERROR;

  return null;
};
