import { createFeatureSelector, createSelector } from '@ngrx/store';
import { SuppliersState } from './suppliers.reducer';

export const selectSuppliersState = createFeatureSelector<SuppliersState>('suppliers');
export const selectSupplierPage   = createSelector(selectSuppliersState, s => s.page);
export const selectSupplierList   = createSelector(selectSupplierPage, p => p?.content ?? []);
export const selectTotalElements  = createSelector(selectSupplierPage, p => p?.totalElements ?? 0);
export const selectSuppLoading    = createSelector(selectSuppliersState, s => s.isLoading);
export const selectSuppSaving     = createSelector(selectSuppliersState, s => s.isSaving);
export const selectSuppError      = createSelector(selectSuppliersState, s => s.error);
export const selectSelectedId     = createSelector(selectSuppliersState, s => s.selectedId);
