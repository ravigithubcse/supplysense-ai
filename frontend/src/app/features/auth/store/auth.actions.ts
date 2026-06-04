import { createAction, props } from '@ngrx/store';
import { User } from '../../../core/models/models';

export const login = createAction('[Auth] Login',
  props<{ email: string; password: string }>());

export const loginSuccess = createAction('[Auth] Login Success',
  props<{ user: User; accessToken: string; refreshToken: string }>());

export const loginFailure = createAction('[Auth] Login Failure',
  props<{ error: string }>());

export const logout = createAction('[Auth] Logout');
export const logoutSuccess = createAction('[Auth] Logout Success');

export const restoreSession = createAction('[Auth] Restore Session');
export const sessionRestored = createAction('[Auth] Session Restored',
  props<{ user: User; accessToken: string }>());
