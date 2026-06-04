import { ActionReducerMap, MetaReducer } from '@ngrx/store';
import { authReducer, AuthState } from '../../features/auth/store/auth.reducer';
import { dashboardReducer, DashboardState } from '../../features/dashboard/store/dashboard.reducer';
import { suppliersReducer, SuppliersState } from '../../features/suppliers/store/suppliers.reducer';
import { riskReducer, RiskState } from '../../features/risk-map/store/risk.reducer';
import { alertsReducer, AlertsState } from '../../features/alerts/store/alerts.reducer';

export interface AppState {
  auth:      AuthState;
  dashboard: DashboardState;
  suppliers: SuppliersState;
  risk:      RiskState;
  alerts:    AlertsState;
}

export const reducers: ActionReducerMap<AppState> = {
  auth:      authReducer,
  dashboard: dashboardReducer,
  suppliers: suppliersReducer,
  risk:      riskReducer,
  alerts:    alertsReducer,
};

export const metaReducers: MetaReducer<AppState>[] = [];
