import { createReducer, on } from '@ngrx/store';
import { RiskDashboardStats, RiskScore, RiskAlert } from '../../../core/models/models';
import * as DashboardActions from './dashboard.actions';

export interface DashboardState {
  stats:        RiskDashboardStats | null;
  scores:       RiskScore[];
  recentAlerts: RiskAlert[];
  isLoading:    boolean;
  error:        string | null;
  lastUpdated:  string | null;
}

const initialState: DashboardState = {
  stats: null, scores: [], recentAlerts: [],
  isLoading: false, error: null, lastUpdated: null,
};

export const dashboardReducer = createReducer(
  initialState,
  on(DashboardActions.loadDashboard,        state => ({ ...state, isLoading: true, error: null })),
  on(DashboardActions.loadDashboardSuccess, (state, { stats, scores, recentAlerts }) =>
    ({ ...state, stats, scores, recentAlerts, isLoading: false, lastUpdated: new Date().toISOString() })),
  on(DashboardActions.loadDashboardFailure, (state, { error }) =>
    ({ ...state, isLoading: false, error })),
  on(DashboardActions.updateRiskScore,      (state, { supplierId, score, riskLevel }) => ({
    ...state,
    scores: state.scores.map(s =>
      s.supplierId === supplierId ? { ...s, compositeScore: score, riskLevel: riskLevel as any } : s
    ),
  })),
);
