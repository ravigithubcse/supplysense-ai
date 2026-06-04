import { createAction, props } from '@ngrx/store';
import { RiskDashboardStats, RiskScore, RiskAlert } from '../../../core/models/models';

export const loadDashboard    = createAction('[Dashboard] Load');
export const loadDashboardSuccess = createAction('[Dashboard] Load Success',
  props<{ stats: RiskDashboardStats; scores: RiskScore[]; recentAlerts: RiskAlert[] }>());
export const loadDashboardFailure = createAction('[Dashboard] Load Failure',
  props<{ error: string }>());
export const updateRiskScore  = createAction('[Dashboard] Update Risk Score (WS)',
  props<{ supplierId: string; score: number; riskLevel: string }>());
