import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { RiskScore } from '../../core/models/models';
import { loadRiskScores, selectAllScores, selectRiskLoading } from '../risk-map/store/risk.reducer';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './analytics.component.html',
})
export class AnalyticsComponent implements OnInit {
  private readonly store = inject(Store);

  scores$:    Observable<RiskScore[]> = this.store.select(selectAllScores);
  isLoading$: Observable<boolean>     = this.store.select(selectRiskLoading);

  ngOnInit(): void { this.store.dispatch(loadRiskScores()); }

  getRiskLevelCounts(scores: RiskScore[]): { level: string; count: number; pct: number }[] {
    const levels = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'MINIMAL'];
    const total  = scores.length || 1;
    return levels.map(level => ({
      level,
      count: scores.filter(s => s.riskLevel === level).length,
      pct:   Math.round(scores.filter(s => s.riskLevel === level).length / total * 100),
    }));
  }

  getBarColor(level: string): string {
    return { CRITICAL: 'bg-red-500', HIGH: 'bg-orange-500', MEDIUM: 'bg-yellow-500', LOW: 'bg-green-500', MINIMAL: 'bg-emerald-500' }[level] ?? 'bg-slate-500';
  }

  getTextColor(level: string): string {
    return { CRITICAL: 'text-red-400', HIGH: 'text-orange-400', MEDIUM: 'text-yellow-400', LOW: 'text-green-400', MINIMAL: 'text-emerald-400' }[level] ?? 'text-slate-400';
  }

  getAvgScore(scores: RiskScore[]): number {
    if (!scores.length) return 0;
    return Math.round(scores.reduce((sum, s) => sum + s.compositeScore, 0) / scores.length * 10) / 10;
  }

  getMaxScore(scores: RiskScore[]): number {
    return scores.length ? Math.max(...scores.map(s => s.compositeScore)) : 0;
  }
}
