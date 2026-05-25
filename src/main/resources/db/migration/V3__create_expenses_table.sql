CREATE TABLE expenses (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount      DECIMAL(12,2) NOT NULL,
    currency    VARCHAR(3)   NOT NULL DEFAULT 'TRY',
    description VARCHAR(255) NOT NULL,
    category    VARCHAR(50)  NOT NULL,
    type        VARCHAR(10)  NOT NULL DEFAULT 'expense',
    date        DATE         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_expenses_user_id ON expenses(user_id);
CREATE INDEX idx_expenses_user_date ON expenses(user_id, date DESC);
