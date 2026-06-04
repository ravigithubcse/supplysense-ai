import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AuthState } from './auth.reducer';

export const selectAuthState = createFeatureSelector<AuthState>('auth');
export const selectUser        = createSelector(selectAuthState, s => s.user);
export const selectToken       = createSelector(selectAuthState, s => s.accessToken);
export const selectAuthLoading = createSelector(selectAuthState, s => s.isLoading);
export const selectAuthError   = createSelector(selectAuthState, s => s.error);
export const selectIsLoggedIn  = createSelector(selectAuthState, s => !!s.accessToken && !!s.user);
export const selectUserRoles   = createSelector(selectUser, u => u?.roles ?? []);
export const selectOrgId       = createSelector(selectUser, u => u?.organizationId ?? null);
