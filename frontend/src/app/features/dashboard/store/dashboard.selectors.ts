import { createFeatureSelector, createSelector } from '@ngrx/store';
import { DashboardState } from './dashboard.reducer';

export const selectDashboard     = createFeatureSelector<DashboardState>('dashboard');
export const selectStats         = createSelector(selectDashboard, s => s.stats);
export const selectScores        = createSelector(selectDashboard, s => s.scores);
export const selectRecentAlerts  = createSelector(selectDashboard, s => s.recentAlerts);
export const selectDashLoading   = createSelector(selectDashboard, s => s.isLoading);
export const selectLastUpdated   = createSelector(selectDashboard, s => s.lastUpdated);
export const selectCriticalCount = createSelector(selectScores, scores =>
  scores.filter(s => s.riskLevel === 'CRITICAL').length);
export const selectHighCount     = createSelector(selectScores, scores =>
  scores.filter(s => s.riskLevel === 'HIGH').length);
