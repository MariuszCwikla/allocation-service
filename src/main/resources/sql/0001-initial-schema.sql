CREATE TABLE equipments (
    id               UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    type             VARCHAR(20)     NOT NULL,
    brand            VARCHAR(100)    NOT NULL,
    model            VARCHAR(200)    NOT NULL,
    state            VARCHAR(20) NOT NULL DEFAULT 'available',
    condition_score  NUMERIC(3, 2)   NOT NULL,
    purchase_date    DATE            NOT NULL,
    retired_at         TIMESTAMPTZ,
    retirement_reason  VARCHAR(20),
    reserved_at        TIMESTAMPTZ,
    confirmed_at       TIMESTAMPTZ,
    version          INT             NOT NULL,
    created_at       TIMESTAMPTZ     NOT NULL,
    updated_at       TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_equipments_type_brand_condition_score ON equipments (state, type, brand, condition_score);
CREATE INDEX idx_equipments_type_condition_score ON equipments (state, type, condition_score);

CREATE TABLE allocation_requests (
     id             UUID             PRIMARY KEY,
     employee_id    UUID             NOT NULL,
     state          VARCHAR(20)      NOT NULL,
     failure_reason VARCHAR(100)     NULL,
     allocated_at   TIMESTAMPTZ      NULL,
     confirmed_at   TIMESTAMPTZ      NULL,
     cancelled_at   TIMESTAMPTZ      NULL,
     version        INT              NOT NULL,
     created_at     TIMESTAMPTZ      NOT NULL,
     updated_at     TIMESTAMPTZ      NOT NULL
);

CREATE INDEX idx_allocation_requests_employee ON allocation_requests (employee_id);
CREATE INDEX idx_allocation_requests_state    ON allocation_requests (state);

CREATE TABLE allocation_policy_items (
     id                    UUID           PRIMARY KEY,
     allocation_request_id UUID           NOT NULL REFERENCES allocation_requests (id) ON DELETE CASCADE,
     equipment_type        VARCHAR(20)    NOT NULL,
     min_condition_score   NUMERIC(3, 2),
     preferred_brand       VARCHAR(100)
);

CREATE INDEX idx_policy_items_request ON allocation_policy_items (allocation_request_id);

CREATE TABLE allocation_equipments (
    allocation_request_id UUID NOT NULL REFERENCES allocation_requests (id) ON DELETE CASCADE,
    equipment_id          UUID NOT NULL REFERENCES equipments (id),
    PRIMARY KEY (allocation_request_id, equipment_id)
);

CREATE INDEX idx_alloc_equipments_request   ON allocation_equipments (allocation_request_id);
CREATE INDEX idx_alloc_equipments_equipment ON allocation_equipments (equipment_id);
