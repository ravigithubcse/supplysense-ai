import { Routes } from '@angular/router';

export const SUPPLIER_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./suppliers-list/suppliers-list.component').then(m => m.SuppliersListComponent),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./supplier-detail/supplier-detail.component').then(m => m.SupplierDetailComponent),
  },
];
