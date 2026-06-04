import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { forkJoin, of } from 'rxjs';
import { map, catchError, switchMap } from 'rxjs/operators';
import * as DashboardActions from './dashboard.actions';
import { RiskService } from '../../../core/services/risk.service';

@Injectable()
export class DashboardEffects {
  private readonly actions$ = inject(Actions);
  private readonly riskService = inject(RiskService);

  loadDashboard$ = createEffect(() =>
    this.actions$.pipe(
      ofType(DashboardActions.loadDashboard),
      switchMap(() =>
        forkJoin({
          stats:  this.riskService.getDashboardStats(),
          scores: this.riskService.getLatestScores(),
          alerts: this.riskService.getAlerts({ page: 0, size: 5, status: 'ACTIVE' }),
        }).pipe(
          map(({ stats, scores, alerts }) =>
            DashboardActions.loadDashboardSuccess({
              stats,
              scores,
              recentAlerts: alerts.content,
            })
          ),
          catchError(err =>
            of(DashboardActions.loadDashboardFailure({ error: err.userMessage ?? 'Failed to load dashboard' }))
          )
        )
      )
    )
  );
}
