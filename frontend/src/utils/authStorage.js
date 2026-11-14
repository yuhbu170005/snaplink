export const AUTH_EXPIRED_EVENT = 'snaplink:auth-expired';
const TOKEN_KEY = 'snaplink_jwt_token';
const USER_KEY = 'snaplink_user';

/** “Ghi nhớ” dùng localStorage; phiên tạm dùng sessionStorage của tab hiện tại. */
export function readToken() {
  return sessionStorage.getItem(TOKEN_KEY) || localStorage.getItem(TOKEN_KEY);
}

export function clearSession() {
  for (const storage of [localStorage, sessionStorage]) {
    storage.removeItem(TOKEN_KEY);
    storage.removeItem(USER_KEY);
  }
}

export function saveSession(token, rememberMe = true) {
  clearSession();
  const storage = rememberMe ? localStorage : sessionStorage;
  storage.setItem(TOKEN_KEY, token);
}
