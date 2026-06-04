import { createReducer, on } from '@ngrx/store';
import { Supplier, SupplierPage } from '../../../core/models/models';
import * as SuppliersActions from './suppliers.actions';

export interface SuppliersState {
  page:             SupplierPage | null;
  selectedId:       string | null;
  isLoading:        boolean;
  isSaving:         boolean;
  error:            string | null;
}

const initialState: SuppliersState = {
  page: null, selectedId: null,
  isLoading: false, isSaving: false, error: null,
};

export const suppliersReducer = createReducer(
  initialState,
  on(SuppliersActions.loadSuppliers,          s => ({ ...s, isLoading: true, error: null })),
  on(SuppliersActions.loadSuppliersSuccess,   (s, { page }) => ({ ...s, page, isLoading: false })),
  on(SuppliersActions.loadSuppliersFailure,   (s, { error }) => ({ ...s, isLoading: false, error })),
  on(SuppliersActions.selectSupplier,         (s, { supplierId }) => ({ ...s, selectedId: supplierId })),
  on(SuppliersActions.createSupplier,         s => ({ ...s, isSaving: true })),
  on(SuppliersActions.createSupplierSuccess,  (s, { supplier }) => ({
    ...s, isSaving: false,
    page: s.page ? { ...s.page, content: [supplier, ...s.page.content], totalElements: s.page.totalElements + 1 } : null,
  })),
  on(SuppliersActions.updateSupplierSuccess,  (s, { supplier }) => ({
    ...s, isSaving: false,
    page: s.page ? { ...s.page, content: s.page.content.map(sup => sup.id === supplier.id ? supplier : sup) } : null,
  })),
  on(SuppliersActions.deleteSupplierSuccess,  (s, { id }) => ({
    ...s,
    page: s.page ? { ...s.page, content: s.page.content.filter(sup => sup.id !== id) } : null,
  })),
);
