-- Тестовые объекты OpenData IDE (ТЗ, разделы 19, 34).
CREATE TABLE IF NOT EXISTS opendata_test (
    id BIGSERIAL PRIMARY KEY,
    name TEXT,
    created_at TIMESTAMP DEFAULT now()
);

INSERT INTO opendata_test(name) VALUES ('alpha'), ('beta'), ('gamma');

-- Объекты для проверки Database Explorer (Views / Functions / Sequences / Types / FK / Indexes / Triggers).
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    full_name TEXT,
    profile JSONB,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    amount NUMERIC(12, 2) CHECK (amount >= 0),
    note TEXT,
    attachment BYTEA,
    created_at TIMESTAMP DEFAULT now()
);
CREATE INDEX IF NOT EXISTS orders_user_idx ON orders(user_id);

CREATE TYPE order_status AS ENUM ('new', 'paid', 'shipped');
CREATE SEQUENCE IF NOT EXISTS opendata_seq START 1000;

CREATE OR REPLACE VIEW user_order_totals AS
    SELECT u.id, u.email, coalesce(sum(o.amount), 0) AS total
    FROM users u LEFT JOIN orders o ON o.user_id = u.id
    GROUP BY u.id, u.email;

CREATE OR REPLACE FUNCTION touch_created_at() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    NEW.created_at := coalesce(NEW.created_at, now());
    RETURN NEW;
END $$;

CREATE TRIGGER orders_touch BEFORE INSERT ON orders FOR EACH ROW EXECUTE FUNCTION touch_created_at();

INSERT INTO users(email, full_name, profile) VALUES
    ('ann@example.com', 'Ann', '{"lang": "ru"}'),
    ('bob@example.com', NULL, NULL);
INSERT INTO orders(user_id, amount, note) VALUES (1, 100.50, 'first'), (1, 20, NULL), (2, 0, 'free');

-- Большая таблица для paging / fetch more / cancel.
CREATE TABLE IF NOT EXISTS big_table AS
    SELECT g AS id, md5(g::text) AS value, now() - (g || ' seconds')::interval AS ts
    FROM generate_series(1, 200000) g;
