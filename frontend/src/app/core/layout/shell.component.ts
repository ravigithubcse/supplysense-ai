import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs';
import { User } from '../models/models';
import { selectUser } from '../../features/auth/store/auth.selectors';
import * as AuthActions from '../../features/auth/store/auth.actions';
import { WebSocketService } from '../services/websocket.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.component.html',
})
export class ShellComponent implements OnInit, OnDestroy {
  private readonly store = inject(Store);
  private readonly wsService = inject(WebSocketService);
  private subs = new Subscription();

  user$: Observable<User | null> = this.store.select(selectUser);
  sidebarOpen = true;
  notifCount = 0;

  readonly navItems = [
    { path: '/dashboard',  label: 'Dashboard',  icon: 'chart-bar' },
    { path: '/suppliers',  label: 'Suppliers',  icon: 'building-office' },
    { path: '/risk-map',   label: 'Risk Map',   icon: 'map' },
    { path: '/alerts',     label: 'Alerts',     icon: 'bell' },
    { path: '/analytics',  label: 'Analytics',  icon: 'presentation-chart-line' },
    { path: '/settings',   label: 'Settings',   icon: 'cog-6-tooth' },
  ];

  ngOnInit(): void {
    this.subs.add(
      this.user$.subscribe(user => {
        if (user?.organizationId) this.wsService.connect(user.organizationId);
      })
    );
    this.subs.add(
      this.wsService.getAlertEvents().subscribe(() => this.notifCount++)
    );
  }

  logout(): void { this.store.dispatch(AuthActions.logout()); }
  toggleSidebar(): void { this.sidebarOpen = !this.sidebarOpen; }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
    this.wsService.disconnect();
  }
}
