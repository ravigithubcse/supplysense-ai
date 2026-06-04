import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { RiskAlert } from '../../core/models/models';
import {
  loadAlerts, acknowledgeAlert, resolveAlert,
  selectAlertList, selectAlertTotal, selectAlertsLoading,
} from './store/alerts.reducer';

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './alerts.component.html',
})
export class AlertsComponent implements OnInit {
  private readonly store = inject(Store);

  alerts$:      Observable<RiskAlert[]> = this.store.select(selectAlertList);
  total$:       Observable<number>      = this.store.select(selectAlertTotal);
  isLoading$:   Observable<boolean>     = this.store.select(selectAlertsLoading);

  statusFilter   = new FormControl('ACTIVE');
  severityFilter = new FormControl('');
  currentPage    = 0;
  resolutionMap: Record<string, string> = {};

  readonly statuses   = ['', 'ACTIVE', 'ACKNOWLEDGED', 'RESOLVED'];
  readonly severities = ['', 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'];

  ngOnInit(): void {
    this.loadPage();
    this.statusFilter.valueChanges.subscribe(() => { this.currentPage = 0; this.loadPage(); });
    this.severityFilter.valueChanges.subscribe(() => { this.currentPage = 0; this.loadPage(); });
  }

  loadPage(): void {
    this.store.dispatch(loadAlerts({
      page: this.currentPage, size: 20,
      status:   this.statusFilter.value   || undefined,
      severity: this.severityFilter.value || undefined,
    }));
  }

  acknowledge(alertId: string): void {
    this.store.dispatch(acknowledgeAlert({ alertId }));
  }

  resolve(alertId: string): void {
    const resolution = this.resolutionMap[alertId] || 'Resolved by operator';
    this.store.dispatch(resolveAlert({ alertId, resolution }));
  }

  getSeverityBg(severity: string): string {
    return {
      CRITICAL: 'bg-red-500/10 border-red-500/30 text-red-300',
      HIGH:     'bg-orange-500/10 border-orange-500/30 text-orange-300',
      MEDIUM:   'bg-yellow-500/10 border-yellow-500/30 text-yellow-300',
      LOW:      'bg-green-500/10 border-green-500/30 text-green-300',
      INFO:     'bg-blue-500/10 border-blue-500/30 text-blue-300',
    }[severity] ?? 'bg-slate-700 border-slate-600 text-slate-300';
  }

  getDotColor(severity: string): string {
    return {
      CRITICAL: 'bg-red-500', HIGH: 'bg-orange-500',
      MEDIUM: 'bg-yellow-500', LOW: 'bg-green-500', INFO: 'bg-blue-500',
    }[severity] ?? 'bg-slate-500';
  }

  getStatusBadge(status: string): string {
    return {
      ACTIVE:         'bg-red-500/10 text-red-400 border-red-500/20',
      ACKNOWLEDGED:   'bg-yellow-500/10 text-yellow-400 border-yellow-500/20',
      RESOLVED:       'bg-green-500/10 text-green-400 border-green-500/20',
      DISMISSED:      'bg-slate-700 text-slate-400 border-slate-600',
    }[status] ?? '';
  }

  nextPage(): void { this.currentPage++; this.loadPage(); }
  prevPage(): void { if (this.currentPage > 0) { this.currentPage--; this.loadPage(); } }
  trackById(_: number, a: RiskAlert): string { return a.id; }
}
