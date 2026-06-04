import { createAction, props } from '@ngrx/store';
import { createReducer, on } from '@ngrx/store';
import { createFeatureSelector, createSelector } from '@ngrx/store';
import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, catchError, switchMap } from 'rxjs/operators';
import { RiskAlert, AlertPage } from '../../../core/models/models';
import { RiskService } from '../../../core/services/risk.service';

// ── Actions ────────────────────────────────────────────────────────────────
export const loadAlerts        = createAction('[Alerts] Load',
  props<{ page: number; size: number; status?: string; severity?: string }>());
export const loadAlertsSuccess = createAction('[Alerts] Load Success',
  props<{ alertPage: AlertPage }>());
export const loadAlertsFailure = createAction('[Alerts] Load Failure', props<{ error: string }>());
export const acknowledgeAlert  = createAction('[Alerts] Acknowledge', props<{ alertId: string }>());
export const acknowledgeAlertSuccess = createAction('[Alerts] Acknowledge Success', props<{ alert: RiskAlert }>());
export const resolveAlert      = createAction('[Alerts] Resolve', props<{ alertId: string; resolution: string }>());
export const resolveAlertSuccess = createAction('[Alerts] Resolve Success', props<{ alert: RiskAlert }>());
export const liveAlertAdded    = createAction('[Alerts] Live Added (WS)', props<{ severity: string; supplierId: string }>());

// ── State ─────────────────────────────────────────────────────────────────
export interface AlertsState {
  alertPage:   AlertPage | null;
  isLoading:   boolean;
  error:       string | null;
  liveCount:   number;
}
const initial: AlertsState = { alertPage: null, isLoading: false, error: null, liveCount: 0 };

// ── Reducer ───────────────────────────────────────────────────────────────
export const alertsReducer = createReducer(
  initial,
  on(loadAlerts,              s => ({ ...s, isLoading: true, error: null })),
  on(loadAlertsSuccess,       (s, { alertPage }) => ({ ...s, alertPage, isLoading: false })),
  on(loadAlertsFailure,       (s, { error }) => ({ ...s, isLoading: false, error })),
  on(acknowledgeAlertSuccess, (s, { alert }) => ({
    ...s,
    alertPage: s.alertPage
      ? { ...s.alertPage, content: s.alertPage.content.map(a => a.id === alert.id ? alert : a) }
      : null,
  })),
  on(resolveAlertSuccess,     (s, { alert }) => ({
    ...s,
    alertPage: s.alertPage
      ? { ...s.alertPage, content: s.alertPage.content.map(a => a.id === alert.id ? alert : a) }
      : null,
  })),
  on(liveAlertAdded, s => ({ ...s, liveCount: s.liveCount + 1 })),
);

// ── Selectors ─────────────────────────────────────────────────────────────
const selectAlertsState  = createFeatureSelector<AlertsState>('alerts');
export const selectAlertList    = createSelector(selectAlertsState, s => s.alertPage?.content ?? []);
export const selectAlertTotal   = createSelector(selectAlertsState, s => s.alertPage?.totalElements ?? 0);
export const selectAlertsLoading = createSelector(selectAlertsState, s => s.isLoading);
export const selectLiveCount    = createSelector(selectAlertsState, s => s.liveCount);

// ── Effects ───────────────────────────────────────────────────────────────
@Injectable()
export class AlertsEffects {
  private readonly actions$ = inject(Actions);
  private readonly riskService = inject(RiskService);

  load$ = createEffect(() => this.actions$.pipe(
    ofType(loadAlerts),
    switchMap(({ page, size, status, severity }) =>
      this.riskService.getAlerts({ page, size, status, severity }).pipe(
        map(alertPage => loadAlertsSuccess({ alertPage })),
        catchError(err => of(loadAlertsFailure({ error: err.userMessage ?? 'Load failed' })))
      )
    )
  ));

  acknowledge$ = createEffect(() => this.actions$.pipe(
    ofType(acknowledgeAlert),
    switchMap(({ alertId }) =>
      this.riskService.acknowledgeAlert(alertId).pipe(
        map(alert => acknowledgeAlertSuccess({ alert })),
        catchError(err => of(loadAlertsFailure({ error: err.userMessage ?? 'Failed' })))
      )
    )
  ));

  resolve$ = createEffect(() => this.actions$.pipe(
    ofType(resolveAlert),
    switchMap(({ alertId, resolution }) =>
      this.riskService.resolveAlert(alertId, resolution).pipe(
        map(alert => resolveAlertSuccess({ alert })),
        catchError(err => of(loadAlertsFailure({ error: err.userMessage ?? 'Failed' })))
      )
    )
  ));
}
