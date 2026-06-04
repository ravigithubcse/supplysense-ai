import { Component, inject, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { SupplierService } from '../../../core/services/supplier.service';
import { RiskService } from '../../../core/services/risk.service';
import { Supplier, RiskScoreDetail } from '../../../core/models/models';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-supplier-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './supplier-detail.component.html',
})
export class SupplierDetailComponent implements OnInit {
  @Input() id!: string;

  private readonly supplierService = inject(SupplierService);
  private readonly riskService = inject(RiskService);

  supplier: Supplier | null = null;
  riskDetail: RiskScoreDetail | null = null;
  isLoading = true;
  error: string | null = null;

  ngOnInit(): void {
    forkJoin({
      supplier: this.supplierService.getSupplier(this.id),
      risk: this.riskService.getSupplierScore(this.id),
    }).subscribe({
      next: ({ supplier, risk }) => { this.supplier = supplier; this.riskDetail = risk; this.isLoading = false; },
      error: err => { this.error = err.userMessage ?? 'Failed to load supplier'; this.isLoading = false; },
    });
  }

  getRiskColor(score: number): string {
    if (score >= 80) return 'text-red-400';
    if (score >= 60) return 'text-orange-400';
    if (score >= 40) return 'text-yellow-400';
    return 'text-green-400';
  }

  getScoreBarColor(score: number): string {
    if (score >= 80) return 'bg-red-500';
    if (score >= 60) return 'bg-orange-500';
    if (score >= 40) return 'bg-yellow-500';
    return 'bg-green-500';
  }
}
