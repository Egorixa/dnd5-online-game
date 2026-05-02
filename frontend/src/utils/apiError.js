import { mapErrorToTzMessage } from './errorMessages';

const normalizeFieldName = (raw) => {
  if (!raw) return '';
  let key = String(raw).replace(/^\$\./, '');
  if (key === 'request' || key === '$') return '';
  key = key.replace(/^[A-Z]/, (c) => c.toLowerCase());
  return key;
};

export const parseApiError = (err, fallback = 'Произошла ошибка') => {
  const tzMessage = mapErrorToTzMessage(err);
  const data = err?.response?.data;

  if (!data) {
    return {
      message: tzMessage || err?.message || fallback,
      fields: {},
      code: null,
    };
  }

  const fields = {};
  const messages = [];

  if (Array.isArray(data.details)) {
    for (const d of data.details) {
      if (!d) continue;
      const key = normalizeFieldName(d.field);
      if (key) fields[key] = d.message;
      if (d.message) messages.push(d.message);
    }
  }

  if (data.errors && typeof data.errors === 'object') {
    for (const [rawField, list] of Object.entries(data.errors)) {
      const key = normalizeFieldName(rawField);
      const arr = Array.isArray(list) ? list : [list];
      for (const m of arr) {
        if (!m) continue;
        if (key && !fields[key]) fields[key] = m;
        messages.push(m);
      }
    }
  }

  const message = tzMessage
    || (messages.length ? messages.join('; ') : (data.message || data.title || fallback));

  return { message, fields, code: data.code || null };
};
