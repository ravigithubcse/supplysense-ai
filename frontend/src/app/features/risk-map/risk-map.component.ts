import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { RiskScore } from '../../core/models/models';
import { loadRiskScores, selectAllScores, selectRiskLoading } from './store/risk.reducer';

@Component({
  selector: 'app-risk-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './risk-map.component.html',
})
export class RiskMapComponent implements OnInit {
  private readonly store = inject(Store);

  scores$:    Observable<RiskScore[]> = this.store.select(selectAllScores);
  isLoading$: Observable<boolean>     = this.store.select(selectRiskLoading);

  selectedScore: RiskScore | null = null;
  viewMode: 'map' | 'grid' = 'map';

  ngOnInit(): void {
    this.store.dispatch(loadRiskScores());
  }

  getRiskColor(level: string): string {
    return { CRITICAL: '#ef4444', HIGH: '#f97316', MEDIUM: '#eab308', LOW: '#22c55e', MINIMAL: '#10b981' }[level] ?? '#64748b';
  }

  getRiskTextClass(level: string): string {
    return { CRITICAL: 'text-red-400', HIGH: 'text-orange-400', MEDIUM: 'text-yellow-400', LOW: 'text-green-400', MINIMAL: 'text-emerald-400' }[level] ?? 'text-slate-400';
  }

  selectScore(score: RiskScore): void { this.selectedScore = score === this.selectedScore ? null : score; }
  trackBySupplier(_: number, s: RiskScore): string { return s.supplierId; }
}
