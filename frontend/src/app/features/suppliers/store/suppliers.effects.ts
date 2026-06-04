import { Injectable, inject } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { map, catchError, switchMap } from 'rxjs/operators';
import * as SuppliersActions from './suppliers.actions';
import { SupplierService } from '../../../core/services/supplier.service';

@Injectable()
export class SuppliersEffects {
  private readonly actions$ = inject(Actions);
  private readonly supplierService = inject(SupplierService);

  load$ = createEffect(() => this.actions$.pipe(
    ofType(SuppliersActions.loadSuppliers),
    switchMap(({ page, size, search, status }) =>
      this.supplierService.getSuppliers({ page, size, search, status }).pipe(
        map(p => SuppliersActions.loadSuppliersSuccess({ page: p })),
        catchError(err => of(SuppliersActions.loadSuppliersFailure({ error: err.userMessage ?? 'Load failed' })))
      )
    )
  ));

  create$ = createEffect(() => this.actions$.pipe(
    ofType(SuppliersActions.createSupplier),
    switchMap(({ payload }) =>
      this.supplierService.createSupplier(payload).pipe(
        map(supplier => SuppliersActions.createSupplierSuccess({ supplier })),
        catchError(err => of(SuppliersActions.loadSuppliersFailure({ error: err.userMessage ?? 'Create failed' })))
      )
    )
  ));

  update$ = createEffect(() => this.actions$.pipe(
    ofType(SuppliersActions.updateSupplier),
    switchMap(({ id, payload }) =>
      this.supplierService.updateSupplier(id, payload).pipe(
        map(supplier => SuppliersActions.updateSupplierSuccess({ supplier })),
        catchError(err => of(SuppliersActions.loadSuppliersFailure({ error: err.userMessage ?? 'Update failed' })))
      )
    )
  ));

  delete$ = createEffect(() => this.actions$.pipe(
    ofType(SuppliersActions.deleteSupplier),
    switchMap(({ id }) =>
      this.supplierService.deleteSupplier(id).pipe(
        map(() => SuppliersActions.deleteSupplierSuccess({ id })),
        catchError(err => of(SuppliersActions.loadSuppliersFailure({ error: err.userMessage ?? 'Delete failed' })))
      )
    )
  ));
}
