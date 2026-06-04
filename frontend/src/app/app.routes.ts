import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES),
  },
  {
    path: '',
    loadComponent: () =>
      import('./core/layout/shell.component').then(m => m.ShellComponent),
    canActivate: [authGuard],
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
        title: 'Dashboard — SupplySense AI',
      },
      {
        path: 'suppliers',
        loadChildren: () =>
          import('./features/suppliers/suppliers.routes').then(m => m.SUPPLIER_ROUTES),
        title: 'Suppliers — SupplySense AI',
      },
      {
        path: 'risk-map',
        loadComponent: () =>
          import('./features/risk-map/risk-map.component').then(m => m.RiskMapComponent),
        title: 'Risk Map — SupplySense AI',
      },
      {
        path: 'alerts',
        loadComponent: () =>
          import('./features/alerts/alerts.component').then(m => m.AlertsComponent),
        title: 'Alerts — SupplySense AI',
      },
      {
        path: 'analytics',
        loadComponent: () =>
          import('./features/analytics/analytics.component').then(m => m.AnalyticsComponent),
        title: 'Analytics — SupplySense AI',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'MANAGER', 'ANALYST'] },
      },
      {
        path: 'settings',
        loadComponent: () =>
          import('./features/settings/settings.component').then(m => m.SettingsComponent),
        title: 'Settings — SupplySense AI',
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
