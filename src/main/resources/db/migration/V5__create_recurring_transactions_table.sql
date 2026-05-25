CREATE TABLE recurring_transactions (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount      DECIMAL(12,2) NOT NULL,
    currency    VARCHAR(3)   NOT NULL DEFAULT 'TRY',
    description VARCHAR(255) NOT NULL,
    category    VARCHAR(50)  NOT NULL,
    type        VARCHAR(10)  NOT NULL DEFAULT 'expense',
    frequency   VARCHAR(10)  NOT NULL DEFAULT 'monthly',
    next_date   DATE         NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT true,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_recurring_user_id ON recurring_transactions(user_id);
