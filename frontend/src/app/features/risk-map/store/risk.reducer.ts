import { createAction, props } from '@ngrx/store';
import { createReducer, on } from '@ngrx/store';
import { createFeatureSelector, createSelector } from '@ngrx/store';
import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, catchError, switchMap } from 'rxjs/operators';
import { RiskScore } from '../../../core/models/models';
import { RiskService } from '../../../core/services/risk.service';

// ── Actions ──────────────────────────────────────────────────────────────────
export const loadRiskScores        = createAction('[Risk] Load Scores');
export const loadRiskScoresSuccess = createAction('[Risk] Load Scores Success', props<{ scores: RiskScore[] }>());
export const loadRiskScoresFailure = createAction('[Risk] Load Scores Failure', props<{ error: string }>());
export const liveRiskUpdate        = createAction('[Risk] Live Update (WS)',
  props<{ supplierId: string; score: number; riskLevel: string }>());

// ── State ─────────────────────────────────────────────────────────────────────
export interface RiskState { scores: RiskScore[]; isLoading: boolean; error: string | null; }
const initial: RiskState = { scores: [], isLoading: false, error: null };

// ── Reducer ───────────────────────────────────────────────────────────────────
export const riskReducer = createReducer(
  initial,
  on(loadRiskScores,        s => ({ ...s, isLoading: true })),
  on(loadRiskScoresSuccess, (s, { scores }) => ({ ...s, scores, isLoading: false })),
  on(loadRiskScoresFailure, (s, { error }) => ({ ...s, isLoading: false, error })),
  on(liveRiskUpdate,        (s, { supplierId, score, riskLevel }) => ({
    ...s, scores: s.scores.map(r =>
      r.supplierId === supplierId ? { ...r, compositeScore: score, riskLevel: riskLevel as any } : r
    ),
  })),
);

// ── Selectors ─────────────────────────────────────────────────────────────────
const selectRiskState = createFeatureSelector<RiskState>('risk');
export const selectAllScores   = createSelector(selectRiskState, s => s.scores);
export const selectRiskLoading = createSelector(selectRiskState, s => s.isLoading);

// ── Effects ───────────────────────────────────────────────────────────────────
@Injectable()
export class RiskEffects {
  private readonly actions$ = inject(Actions);
  private readonly riskService = inject(RiskService);

  load$ = createEffect(() => this.actions$.pipe(
    ofType(loadRiskScores),
    switchMap(() => this.riskService.getLatestScores().pipe(
      map(scores => loadRiskScoresSuccess({ scores })),
      catchError(err => of(loadRiskScoresFailure({ error: err.userMessage ?? 'Load failed' })))
    ))
  ));
}
