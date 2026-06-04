// ── Auth ──────────────────────────────────────────────────────────────────────

export interface User {
  id:               string;
  firstName:        string;
  lastName:         string;
  email:            string;
  roles:            string[];
  organizationId:   string | null;
  organizationName: string | null;
  lastLoginAt:      string | null;
}

export interface AuthState {
  user:         User | null;
  accessToken:  string | null;
  refreshToken: string | null;
  isLoading:    boolean;
  error:        string | null;
}

// ── Supplier ──────────────────────────────────────────────────────────────────

export type SupplierStatus = 'ACTIVE' | 'INACTIVE' | 'PROBATION' | 'BLACKLISTED' | 'PENDING_REVIEW';

export interface Supplier {
  id:               string;
  name:             string;
  code:             string;
  country:          string;
  city?:            string;
  region?:          string;
  industry?:        string;
  category?:        string;
  status:           SupplierStatus;
  reliabilityScore: number;
  qualityScore:     number;
  deliveryScore:    number;
  latitude?:        number;
  longitude?:       number;
  riskScore?:       number;
  contactEmail?:    string;
  annualSpend?:     number;
  createdAt?:       string;
}

export interface SupplierPage {
  content:       Supplier[];
  totalElements: number;
  totalPages:    number;
  size:          number;
  number:        number;
}

// ── Risk ──────────────────────────────────────────────────────────────────────

export type RiskLevel = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'MINIMAL';

export interface RiskScore {
  supplierId:       string;
  compositeScore:   number;
  riskLevel:        RiskLevel;
  forecastScore7d:  number;
  forecastScore30d: number;
  calculatedAt:     string;
}

export interface RiskScoreDetail extends RiskScore {
  id:                string;
  geopoliticalScore: number;
  weatherScore:      number;
  financialScore:    number;
  logisticsScore:    number;
  sentimentScore:    number;
  confidence:        number;
  modelVersion:      string;
  history:           { score: number; riskLevel: RiskLevel; timestamp: string }[];
}

export interface RiskDashboardStats {
  totalSuppliers:   number;
  criticalRiskCount: number;
  highRiskCount:    number;
  averageRiskScore: number;
  activeAlerts:     number;
  criticalAlerts:   number;
}

// ── Alert ─────────────────────────────────────────────────────────────────────

export type AlertSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'INFO';
export type AlertStatus   = 'ACTIVE' | 'ACKNOWLEDGED' | 'RESOLVED' | 'DISMISSED';
export type AlertType     = 'GEOPOLITICAL' | 'WEATHER' | 'FINANCIAL' | 'LOGISTICS' | 'SENTIMENT' | 'ANOMALY' | 'FORECAST';

export interface RiskAlert {
  id:              string;
  supplierId:      string;
  title:           string;
  description:     string;
  severity:        AlertSeverity;
  type:            AlertType;
  status:          AlertStatus;
  affectedCountry: string | null;
  createdAt:       string;
  resolvedAt?:     string;
}

export interface AlertPage {
  content:       RiskAlert[];
  totalElements: number;
  totalPages:    number;
  size:          number;
  number:        number;
}

// ── Notification ──────────────────────────────────────────────────────────────

export interface AppNotification {
  id:        string;
  title:     string;
  message:   string;
  severity:  AlertSeverity;
  status:    'PENDING' | 'SENT' | 'READ';
  createdAt: string;
}

// ── Pagination ────────────────────────────────────────────────────────────────

export interface PageRequest {
  page:      number;
  size:      number;
  sortBy?:   string;
  direction?: 'asc' | 'desc';
}
