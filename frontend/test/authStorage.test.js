import test from 'node:test';
import assert from 'node:assert/strict';
import { readToken, saveSession, clearSession } from '../src/utils/authStorage.js';

function storage() {
  const values = new Map();
  return { getItem: (key) => values.get(key) ?? null, setItem: (key, value) => values.set(key, value), removeItem: (key) => values.delete(key) };
}

test('ghi nhớ đăng nhập và phiên tab không để lại token cũ', () => {
  globalThis.localStorage = storage();
  globalThis.sessionStorage = storage();
  saveSession('persistent', true);
  assert.equal(localStorage.getItem('snaplink_jwt_token'), 'persistent');
  saveSession('temporary', false);
  assert.equal(localStorage.getItem('snaplink_jwt_token'), null);
  assert.equal(readToken(), 'temporary');
  clearSession();
  assert.equal(readToken(), null);
});
