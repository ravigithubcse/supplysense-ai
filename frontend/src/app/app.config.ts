import { ApplicationConfig, isDevMode } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideStore } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { provideRouterStore } from '@ngrx/router-store';
import { provideStoreDevtools } from '@ngrx/store-devtools';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { reducers, metaReducers } from './core/store/reducers';
import { AuthEffects } from './features/auth/store/auth.effects';
import { DashboardEffects } from './features/dashboard/store/dashboard.effects';
import { SuppliersEffects } from './features/suppliers/store/suppliers.effects';
import { RiskEffects } from './features/risk-map/store/risk.effects';
import { AlertsEffects } from './features/alerts/store/alerts.effects';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
    provideAnimations(),
    provideStore(reducers, { metaReducers }),
    provideEffects([
      AuthEffects,
      DashboardEffects,
      SuppliersEffects,
      RiskEffects,
      AlertsEffects,
    ]),
    provideRouterStore(),
    provideStoreDevtools({
      maxAge: 25,
      logOnly: !isDevMode(),
      autoPause: true,
    }),
  ],
};
