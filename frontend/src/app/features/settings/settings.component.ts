import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { selectUser } from '../auth/store/auth.selectors';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './settings.component.html',
})
export class SettingsComponent {
  private readonly store = inject(Store);
  private readonly fb = inject(FormBuilder);

  user$ = this.store.select(selectUser);
  saved = false;
  activeTab: 'profile' | 'notifications' | 'api' = 'profile';

  profileForm = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName:  ['', Validators.required],
    email:     ['', [Validators.required, Validators.email]],
  });

  notifForm = this.fb.nonNullable.group({
    emailAlerts:    [true],
    slackAlerts:    [false],
    criticalOnly:   [false],
    weeklyDigest:   [true],
  });

  saveProfile(): void { this.saved = true; setTimeout(() => this.saved = false, 3000); }
  saveNotifications(): void { this.saved = true; setTimeout(() => this.saved = false, 3000); }
}
