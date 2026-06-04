import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { RiskScore, RiskScoreDetail, RiskDashboardStats, RiskAlert, AlertPage } from '../models/models';

@Injectable({ providedIn: 'root' })
export class RiskService {
  private readonly api = inject(ApiService);

  getLatestScores(): Observable<RiskScore[]> {
    return this.api.get<RiskScore[]>('/api/v1/risk/scores');
  }

  getSupplierScore(supplierId: string): Observable<RiskScoreDetail> {
    return this.api.get<RiskScoreDetail>(`/api/v1/risk/scores/${supplierId}`);
  }

  calculateRisk(supplierId: string, country: string, industry?: string): Observable<RiskScoreDetail> {
    return this.api.post<RiskScoreDetail>('/api/v1/risk/scores/calculate', { supplierId, country, industry });
  }

  getDashboardStats(): Observable<RiskDashboardStats> {
    return this.api.get<RiskDashboardStats>('/api/v1/risk/dashboard');
  }

  getAlerts(params: { page?: number; size?: number; status?: string; severity?: string }): Observable<AlertPage> {
    return this.api.get<AlertPage>('/api/v1/risk/alerts', params);
  }

  acknowledgeAlert(alertId: string): Observable<RiskAlert> {
    return this.api.patch<RiskAlert>(`/api/v1/risk/alerts/${alertId}/acknowledge`, {});
  }

  resolveAlert(alertId: string, resolution: string): Observable<RiskAlert> {
    return this.api.patch<RiskAlert>(`/api/v1/risk/alerts/${alertId}/resolve`, { resolution });
  }

  runWhatIfScenario(supplierId: string, scenarioType: string, impactFactor: number): Observable<any> {
    return this.api.post('/api/v1/risk/scenarios/what-if', { supplierId, scenarioType, impactFactor });
  }
}
