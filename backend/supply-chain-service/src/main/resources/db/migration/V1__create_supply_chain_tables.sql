-- Supply Chain Service Schema V1

CREATE TABLE IF NOT EXISTS supply_chain.suppliers (
    id                  UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    name                VARCHAR(200) NOT NULL,
    code                VARCHAR(50)  NOT NULL,
    description         TEXT,
    country             VARCHAR(100) NOT NULL,
    region              VARCHAR(100),
    city                VARCHAR(100),
    latitude            DECIMAL(10,7),
    longitude           DECIMAL(10,7),
    industry            VARCHAR(100),
    category            VARCHAR(100),
    status              VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    reliability_score   INT          NOT NULL DEFAULT 75,
    quality_score       INT          NOT NULL DEFAULT 75,
    delivery_score      INT          NOT NULL DEFAULT 75,
    annual_spend        DECIMAL(18,2) NOT NULL DEFAULT 0,
    contact_name        VARCHAR(200),
    contact_email       VARCHAR(255),
    contact_phone       VARCHAR(50),
    website             VARCHAR(500),
    org_id              UUID         NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS supply_chain.supplier_products (
    id               UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    supplier_id      UUID         NOT NULL REFERENCES supply_chain.suppliers(id) ON DELETE CASCADE,
    product_name     VARCHAR(200) NOT NULL,
    sku              VARCHAR(100) NOT NULL,
    category         VARCHAR(100),
    unit_price       DECIMAL(15,2),
    lead_time_days   INT,
    min_order_qty    INT,
    active           BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS supply_chain.shipping_routes (
    id               UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    name             VARCHAR(200) NOT NULL,
    origin_country   VARCHAR(100) NOT NULL,
    origin_port      VARCHAR(200),
    dest_country     VARCHAR(100) NOT NULL,
    dest_port        VARCHAR(200),
    transport_mode   VARCHAR(50)  NOT NULL DEFAULT 'SEA',
    avg_transit_days INT,
    org_id           UUID         NOT NULL,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS supply_chain.warehouses (
    id               UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    name             VARCHAR(200) NOT NULL,
    code             VARCHAR(50)  NOT NULL,
    country          VARCHAR(100) NOT NULL,
    city             VARCHAR(100),
    latitude         DECIMAL(10,7),
    longitude        DECIMAL(10,7),
    capacity_sqm     DECIMAL(12,2),
    utilization_pct  INT          NOT NULL DEFAULT 0,
    org_id           UUID         NOT NULL,
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_suppliers_org_id    ON supply_chain.suppliers(org_id);
CREATE INDEX idx_suppliers_status    ON supply_chain.suppliers(status);
CREATE INDEX idx_suppliers_country   ON supply_chain.suppliers(country);
CREATE INDEX idx_routes_org_id       ON supply_chain.shipping_routes(org_id);
CREATE INDEX idx_warehouses_org_id   ON supply_chain.warehouses(org_id);

-- Seed demo suppliers
INSERT INTO supply_chain.suppliers (id, name, code, country, region, city, latitude, longitude, industry, category, reliability_score, quality_score, delivery_score, org_id)
VALUES
  ('10000000-0000-0000-0000-000000000001','Shenzhen Electronics Co','SEC-001','China','Guangdong','Shenzhen',22.5431,114.0579,'Electronics','Manufacturing',82,88,75,'00000000-0000-0000-0000-000000000001'),
  ('10000000-0000-0000-0000-000000000002','Mumbai Textiles Ltd','MTL-001','India','Maharashtra','Mumbai',19.0760,72.8777,'Textiles','Manufacturing',91,93,88,'00000000-0000-0000-0000-000000000001'),
  ('10000000-0000-0000-0000-000000000003','Berlin Precision GmbH','BPG-001','Germany','Bavaria','Munich',48.1351,11.5820,'Automotive','Manufacturing',96,97,94,'00000000-0000-0000-0000-000000000001'),
  ('10000000-0000-0000-0000-000000000004','São Paulo Packaging SA','SPP-001','Brazil','São Paulo','São Paulo',-23.5505,-46.6333,'Packaging','Distribution',78,80,72,'00000000-0000-0000-0000-000000000001'),
  ('10000000-0000-0000-0000-000000000005','Seoul Semiconductors','SSC-001','South Korea','Seoul','Seoul',37.5665,126.9780,'Electronics','Manufacturing',94,96,91,'00000000-0000-0000-0000-000000000001'),
  ('10000000-0000-0000-0000-000000000006','Cape Town Logistics','CTL-001','South Africa','Western Cape','Cape Town',-33.9249,18.4241,'Logistics','Distribution',85,82,90,'00000000-0000-0000-0000-000000000001'),
  ('10000000-0000-0000-0000-000000000007','Toronto Materials Inc','TMI-001','Canada','Ontario','Toronto',43.6532,-79.3832,'Chemicals','Manufacturing',89,91,87,'00000000-0000-0000-0000-000000000001'),
  ('10000000-0000-0000-0000-000000000008','Bangkok Components Co','BCC-001','Thailand','Bangkok','Bangkok',13.7563,100.5018,'Electronics','Manufacturing',76,79,73,'00000000-0000-0000-0000-000000000001')
ON CONFLICT DO NOTHING;
