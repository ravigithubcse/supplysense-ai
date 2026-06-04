import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import * as AuthActions from '../store/auth.actions';
import { selectAuthLoading, selectAuthError } from '../store/auth.selectors';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private readonly store = inject(Store);
  private readonly fb    = inject(FormBuilder);

  isLoading$: Observable<boolean> = this.store.select(selectAuthLoading);
  error$:     Observable<string | null> = this.store.select(selectAuthError);

  showPassword = false;

  form = this.fb.nonNullable.group({
    email:    ['admin@ss.ai', [Validators.required, Validators.email]],
    password: ['Admin1234!', [Validators.required, Validators.minLength(8)]],
  });

  get email()    { return this.form.controls.email; }
  get password() { return this.form.controls.password; }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.store.dispatch(AuthActions.login(this.form.getRawValue()));
  }
}
