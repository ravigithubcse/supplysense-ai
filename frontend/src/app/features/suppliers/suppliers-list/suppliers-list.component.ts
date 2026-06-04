import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, debounceTime, distinctUntilChanged } from 'rxjs';
import { Supplier } from '../../../core/models/models';
import * as SuppliersActions from '../store/suppliers.actions';
import { selectSupplierList, selectTotalElements, selectSuppLoading } from '../store/suppliers.selectors';

@Component({
  selector: 'app-suppliers-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './suppliers-list.component.html',
})
export class SuppliersListComponent implements OnInit {
  private readonly store = inject(Store);

  suppliers$:     Observable<Supplier[]> = this.store.select(selectSupplierList);
  totalElements$: Observable<number>     = this.store.select(selectTotalElements);
  isLoading$:     Observable<boolean>    = this.store.select(selectSuppLoading);

  searchCtrl   = new FormControl('');
  statusFilter = new FormControl('');
  currentPage  = 0;
  pageSize     = 20;

  ngOnInit(): void {
    this.loadPage();
    this.searchCtrl.valueChanges.pipe(debounceTime(400), distinctUntilChanged())
      .subscribe(() => { this.currentPage = 0; this.loadPage(); });
    this.statusFilter.valueChanges.subscribe(() => { this.currentPage = 0; this.loadPage(); });
  }

  loadPage(): void {
    this.store.dispatch(SuppliersActions.loadSuppliers({
      page: this.currentPage,
      size: this.pageSize,
      search: this.searchCtrl.value ?? undefined,
      status: this.statusFilter.value ?? undefined,
    }));
  }

  nextPage(): void { this.currentPage++; this.loadPage(); }
  prevPage(): void { if (this.currentPage > 0) { this.currentPage--; this.loadPage(); } }

  getRiskColor(score?: number): string {
    if (!score) return 'text-slate-400';
    if (score >= 80) return 'text-red-400';
    if (score >= 60) return 'text-orange-400';
    if (score >= 40) return 'text-yellow-400';
    return 'text-green-400';
  }

  getStatusBadge(status: string): string {
    return {
      ACTIVE:          'bg-green-500/10 text-green-400 border-green-500/20',
      INACTIVE:        'bg-slate-500/10 text-slate-400 border-slate-500/20',
      PROBATION:       'bg-yellow-500/10 text-yellow-400 border-yellow-500/20',
      BLACKLISTED:     'bg-red-500/10 text-red-400 border-red-500/20',
      PENDING_REVIEW:  'bg-blue-500/10 text-blue-400 border-blue-500/20',
    }[status] ?? 'bg-slate-700 text-slate-300';
  }

  trackById(_: number, s: Supplier): string { return s.id; }
}
