-- OpenData IDE — контрольные запросы POC (ТЗ, разделы 32–34).
-- Выполнять в SQL Console подключённого PostgreSQL Data Source (Ctrl+Enter — текущий statement).

-- Этап 2: SQL Console
SELECT version();

-- Этап 3: DataGrid (сортировка, фильтр, копирование, изменение ширины колонок)
SELECT
    generate_series AS id,
    md5(generate_series::text) AS value
FROM generate_series(1,100);

-- Этап 4: Editing (таблица уже создана init-скриптом docker/postgres; для внешней БД выполните сами)
CREATE TABLE IF NOT EXISTS opendata_test (
    id BIGSERIAL PRIMARY KEY,
    name TEXT,
    created_at TIMESTAMP DEFAULT now()
);

SELECT * FROM opendata_test ORDER BY id;

-- Completion: после "pub" должен появиться список схем и объектов, после "u." — колонки users
SELECT * FROM pub;
SELECT u.
FROM users u;

-- Навигация: Ctrl+Click по public.users
SELECT * FROM public.users;

-- Ошибка с позицией (ТЗ, раздел 26)
SELECT * FROM no_such_table;

-- Отмена долгого запроса (ТЗ, раздел 25): запустить и нажать Stop / Ctrl+F2
SELECT pg_sleep(60);

-- Типы для проверки рендеринга: NULL, числа, даты, JSON, LOB
SELECT u.id, u.full_name, u.profile, o.amount, o.created_at, o.attachment
FROM users u LEFT JOIN orders o ON o.user_id = u.id;
