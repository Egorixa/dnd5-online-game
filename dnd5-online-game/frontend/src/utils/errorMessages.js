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
  VERSION_CONFLICT: 'Лист был изменён параллельно. Нажмите «Синхронизировать листы» и повторите',
  CHARACTER_FORBIDDEN: 'Редактировать этот лист нельзя',
  CHARACTER_NOT_FOUND: 'Лист персонажа не найден',
  VALIDATION_FAILED: 'Проверьте корректность данных листа',
};

const SUBSTRING_DICT = [
  ['Invalid credentials', 'Неверное имя пользователя или пароль'],
  ['Username already taken', 'Пользователь с таким именем уже существует'],
  ['Passwords do not match', 'Введённые пароли не совпадают'],
  ['was modified concurrently', 'Лист был изменён параллельно. Нажмите «Синхронизировать листы» и повторите'],
  ['Only the owner of the character or the room master may edit it', 'Редактировать этот лист может только его владелец или мастер'],
  ['Only the owner of the character or the room master may delete it', 'Удалить этот лист может только его владелец или мастер'],
  ['Could not allocate a unique room code', 'Не удалось выделить уникальный код комнаты, повторите попытку'],
  ['Room not found', 'Комната не найдена'],
  ['Room is not active', 'Сессия неактивна'],
  ['Room is finished', 'Сессия завершена'],
  ['Already joined', 'Вы уже подключены к этой комнате'],
  ['Master cannot join own room as player', 'Мастер не может подключиться к своей же комнате как игрок'],
  ['Character not found', 'Лист персонажа не найден'],
  ['Validation failed', 'Проверьте корректность данных листа'],
  ['Name must be 1-50 characters', 'Имя должно быть от 1 до 50 символов'],
  ['Up to 20 attacks', 'Можно добавить не более 20 атак'],
  ['Up to 100 spells', 'Можно добавить не более 100 заклинаний'],
  ['must be PUBLIC or PRIVATE', 'Тип доступа должен быть «Публичная» или «Приватная»'],
  ['must be at most 100 characters', 'Длина должна быть не более 100 символов'],
  ['Forbidden', 'Доступ запрещён'],
  ['Unauthorized', 'Требуется авторизация'],
];

const CODE_TO_TZ = {
  VERSION_CONFLICT: TZ_MESSAGES.VERSION_CONFLICT,
  ROOM_NOT_FOUND: TZ_MESSAGES.ROOM_NOT_FOUND,
  ROOM_CODE_CONFLICT: TZ_MESSAGES.ROOM_UNAVAILABLE,
  ALREADY_FINISHED: TZ_MESSAGES.ROOM_UNAVAILABLE,
  ALREADY_JOINED: TZ_MESSAGES.ROOM_UNAVAILABLE,
  CHARACTER_NOT_FOUND: TZ_MESSAGES.CHARACTER_NOT_FOUND,
  USERNAME_TAKEN: TZ_MESSAGES.USERNAME_TAKEN,
  INVALID_CREDENTIALS: TZ_MESSAGES.AUTH_INVALID,
  AUTH_INVALID: TZ_MESSAGES.AUTH_INVALID,
};

export const localizeServerMessage = (raw) => {
  if (!raw || typeof raw !== 'string') return raw;
  for (const [en, ru] of SUBSTRING_DICT) {
    if (raw.toLowerCase().includes(en.toLowerCase())) return ru;
  }
  return raw;
};

export const extractApiError = (err) => {
  if (!err) return null;
  if (err.code === 'ERR_NETWORK' || err.message === 'Network Error') return TZ_MESSAGES.NETWORK_OFFLINE;
  if (err.code === 'ECONNABORTED' || /timeout/i.test(err.message || '')) return TZ_MESSAGES.NETWORK_TIMEOUT;
  const data = err.response?.data;
  const code = data?.code;
  if (code && CODE_TO_TZ[code]) return CODE_TO_TZ[code];
  const detail = data?.details?.[0]?.message;
  if (detail) return localizeServerMessage(detail);
  if (data?.message) return localizeServerMessage(data.message);
  if (err.response?.status >= 500) return TZ_MESSAGES.SERVER_ERROR;
  return localizeServerMessage(err.message) || 'Не удалось выполнить запрос';
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
