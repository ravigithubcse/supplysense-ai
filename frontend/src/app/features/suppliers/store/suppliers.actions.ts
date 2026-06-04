// ── actions ──────────────────────────────────────────────────────────────────
import { createAction, props } from '@ngrx/store';
import { Supplier, SupplierPage } from '../../../core/models/models';

export const loadSuppliers = createAction('[Suppliers] Load',
  props<{ page: number; size: number; search?: string; status?: string }>());
export const loadSuppliersSuccess = createAction('[Suppliers] Load Success',
  props<{ page: SupplierPage }>());
export const loadSuppliersFailure = createAction('[Suppliers] Load Failure',
  props<{ error: string }>());
export const selectSupplier = createAction('[Suppliers] Select',
  props<{ supplierId: string }>());
export const createSupplier = createAction('[Suppliers] Create',
  props<{ payload: Partial<Supplier> }>());
export const createSupplierSuccess = createAction('[Suppliers] Create Success',
  props<{ supplier: Supplier }>());
export const updateSupplier = createAction('[Suppliers] Update',
  props<{ id: string; payload: Partial<Supplier> }>());
export const updateSupplierSuccess = createAction('[Suppliers] Update Success',
  props<{ supplier: Supplier }>());
export const deleteSupplier = createAction('[Suppliers] Delete',
  props<{ id: string }>());
export const deleteSupplierSuccess = createAction('[Suppliers] Delete Success',
  props<{ id: string }>());
