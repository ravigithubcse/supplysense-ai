import { Injectable, OnDestroy, inject } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Store } from '@ngrx/store';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

export interface WsRiskUpdate {
  type:       string;
  supplierId: string;
  score:      number;
  riskLevel:  string;
  timestamp:  number;
}

export interface WsAlertEvent {
  type:       string;
  supplierId: string;
  severity:   string;
  timestamp:  number;
}

@Injectable({ providedIn: 'root' })
export class WebSocketService implements OnDestroy {
  private client!: Client;
  private subscriptions: StompSubscription[] = [];

  private readonly riskUpdates$ = new Subject<WsRiskUpdate>();
  private readonly alertEvents$  = new Subject<WsAlertEvent>();
  private readonly authService   = inject(AuthService);

  connect(orgId: string): void {
    this.client = new Client({
      webSocketFactory: () => new SockJS(environment.wsUrl),
      connectHeaders: {
        Authorization: `Bearer ${this.authService.getAccessToken() ?? ''}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        console.log('[WS] Connected to SupplySense event stream');
        this.subscribe(orgId);
      },
      onDisconnect: () => console.log('[WS] Disconnected'),
      onStompError: (frame) => console.error('[WS] STOMP error', frame),
    });

    this.client.activate();
  }

  private subscribe(orgId: string): void {
    const riskSub = this.client.subscribe(
      `/topic/risk-updates/${orgId}`,
      (msg: IMessage) => {
        try { this.riskUpdates$.next(JSON.parse(msg.body)); } catch { /* ignore */ }
      }
    );

    const alertSub = this.client.subscribe(
      `/topic/alerts/${orgId}`,
      (msg: IMessage) => {
        try { this.alertEvents$.next(JSON.parse(msg.body)); } catch { /* ignore */ }
      }
    );

    this.subscriptions.push(riskSub, alertSub);
  }

  getRiskUpdates(): Observable<WsRiskUpdate>  { return this.riskUpdates$.asObservable(); }
  getAlertEvents(): Observable<WsAlertEvent>  { return this.alertEvents$.asObservable(); }

  disconnect(): void {
    this.subscriptions.forEach(s => s.unsubscribe());
    this.subscriptions = [];
    if (this.client?.active) this.client.deactivate();
  }

  ngOnDestroy(): void { this.disconnect(); }
}
