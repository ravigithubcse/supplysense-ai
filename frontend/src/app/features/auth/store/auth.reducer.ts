import { createReducer, on } from '@ngrx/store';
import { User } from '../../../core/models/models';
import * as AuthActions from './auth.actions';

export interface AuthState {
  user:         User | null;
  accessToken:  string | null;
  refreshToken: string | null;
  isLoading:    boolean;
  error:        string | null;
}

const initialState: AuthState = {
  user:         null,
  accessToken:  null,
  refreshToken: null,
  isLoading:    false,
  error:        null,
};

export const authReducer = createReducer(
  initialState,
  on(AuthActions.login,          state => ({ ...state, isLoading: true, error: null })),
  on(AuthActions.loginSuccess,   (state, { user, accessToken, refreshToken }) =>
    ({ ...state, user, accessToken, refreshToken, isLoading: false, error: null })),
  on(AuthActions.loginFailure,   (state, { error }) =>
    ({ ...state, isLoading: false, error })),
  on(AuthActions.logoutSuccess,  () => initialState),
  on(AuthActions.sessionRestored,(state, { user, accessToken }) =>
    ({ ...state, user, accessToken })),
);
