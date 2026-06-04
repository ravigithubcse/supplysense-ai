import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Supplier, SupplierPage } from '../models/models';

@Injectable({ providedIn: 'root' })
export class SupplierService {
  private readonly api = inject(ApiService);

  getSuppliers(params: { page?: number; size?: number; search?: string; status?: string; sortBy?: string; direction?: string }): Observable<SupplierPage> {
    return this.api.get<SupplierPage>('/api/v1/suppliers', params);
  }

  getSupplier(id: string): Observable<Supplier> {
    return this.api.get<Supplier>(`/api/v1/suppliers/${id}`);
  }

  createSupplier(payload: Partial<Supplier>): Observable<Supplier> {
    return this.api.post<Supplier>('/api/v1/suppliers', payload);
  }

  updateSupplier(id: string, payload: Partial<Supplier>): Observable<Supplier> {
    return this.api.patch<Supplier>(`/api/v1/suppliers/${id}`, payload);
  }

  deleteSupplier(id: string): Observable<void> {
    return this.api.delete<void>(`/api/v1/suppliers/${id}`);
  }

  getStats(): Observable<{ totalActive: number; totalInactive: number; onProbation: number; countries: string[] }> {
    return this.api.get('/api/v1/suppliers/stats');
  }
}
