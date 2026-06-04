import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { RiskDashboardStats, RiskScore, RiskAlert } from '../../core/models/models';
import * as DashboardActions from './store/dashboard.actions';
import {
  selectStats, selectScores, selectRecentAlerts,
  selectDashLoading, selectLastUpdated,
} from './store/dashboard.selectors';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  private readonly store = inject(Store);

  stats$:        Observable<RiskDashboardStats | null> = this.store.select(selectStats);
  scores$:       Observable<RiskScore[]>               = this.store.select(selectScores);
  recentAlerts$: Observable<RiskAlert[]>               = this.store.select(selectRecentAlerts);
  isLoading$:    Observable<boolean>                   = this.store.select(selectDashLoading);
  lastUpdated$:  Observable<string | null>             = this.store.select(selectLastUpdated);

  ngOnInit(): void {
    this.store.dispatch(DashboardActions.loadDashboard());
  }

  getRiskColor(level: string): string {
    return { CRITICAL: 'text-red-400', HIGH: 'text-orange-400',
             MEDIUM: 'text-yellow-400', LOW: 'text-green-400',
             MINIMAL: 'text-emerald-400' }[level] ?? 'text-slate-400';
  }

  getRiskBg(level: string): string {
    return { CRITICAL: 'bg-red-500/10 border-red-500/20',
             HIGH:     'bg-orange-500/10 border-orange-500/20',
             MEDIUM:   'bg-yellow-500/10 border-yellow-500/20',
             LOW:      'bg-green-500/10 border-green-500/20',
             MINIMAL:  'bg-emerald-500/10 border-emerald-500/20' }[level] ?? 'bg-slate-800';
  }

  getSeverityColor(severity: string): string {
    return { CRITICAL: 'bg-red-500',    HIGH: 'bg-orange-500',
             MEDIUM:   'bg-yellow-500', LOW:  'bg-green-500',
             INFO:     'bg-blue-500' }[severity] ?? 'bg-slate-500';
  }

  getRiskBarWidth(score: number): string { return `${Math.min(score, 100)}%`; }
  getRiskBarColor(score: number): string {
    if (score >= 80) return 'bg-red-500';
    if (score >= 60) return 'bg-orange-500';
    if (score >= 40) return 'bg-yellow-500';
    if (score >= 20) return 'bg-green-500';
    return 'bg-emerald-500';
  }

  trackBySupplier(_: number, s: RiskScore): string { return s.supplierId; }
  trackByAlert(_: number, a: RiskAlert): string { return a.id; }
}
