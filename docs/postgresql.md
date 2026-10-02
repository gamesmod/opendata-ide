# PostgreSQL

Остальные СУБД (Cloudberry, ClickHouse, Dremio) описаны в [`databases.md`](databases.md).

## Тестовый стенд

```powershell
docker compose -f docker\docker-compose.yml up -d --wait
```

| Параметр | Значение |
|---|---|
| Host / Port | `localhost` / `54329` (нестандартный порт, чтобы не конфликтовать с локальной PostgreSQL) |
| Database | `opendata` |
| User / Password | `opendata` / `opendata` |
| JDBC URL | `jdbc:postgresql://localhost:54329/opendata` |

Init-скрипт `docker/postgres/init/01-opendata.sql` создаёт:

- `opendata_test` — контрольную таблицу из разделов 19 и 34 ТЗ;
- `users` и `orders` с PK, FK, UNIQUE, CHECK, индексом, триггером, JSONB и BYTEA (для проверки Explorer, JSON и LOB);
- `user_order_totals` (view), `touch_created_at()` (function), `opendata_seq` (sequence), `order_status` (enum type);
- `big_table` на 200 000 строк (для проверки постраничной загрузки и отмены).

Сброс стенда: `docker compose -f docker\docker-compose.yml down -v`.

## Источник данных в IDE

Окно **Database** → **+** → тип **PostgreSQL**. Заполните хост, порт, базу, пользователя и пароль из таблицы выше →
**Test Connection** → **OK**. Пароль сохраняется в штатном `PasswordSafe` IntelliJ.

Драйвер `postgresql-42.7.13.jar` уже входит в дистрибутив (`plugins/opendata-db/drivers`), поэтому сеть не нужна.
Свойства по умолчанию:

- `stringtype=unspecified`: значения, введённые в DataGrid строкой, сервер приводит к типу колонки;
- `ApplicationName=OpenData IDE`.

Пользовательские свойства (поле «Свойства», `ключ=значение;…`) имеют приоритет. Например, SSL: `ssl=true;sslmode=verify-full`.

## Ошибки (ТЗ, раздел 26)

Из `ServerErrorMessage` выводятся ERROR, SQLSTATE, Position, Detail, Hint и Constraint. Позиция подсвечивается
в редакторе консоли. Проверка: тест `testErrorWithPositionAndScriptResults`.

Пример для ручной проверки: `INSERT INTO orders(user_id, amount) VALUES (999, -1);`. Ожидается нарушение
CHECK или FK с заполненным полем Constraint.

## Переменные тестов

| Переменная | По умолчанию |
|---|---|
| `OPENDATA_PG_URL` | не задана: тесты PostgreSQL пропускаются (`test.* --with-docker` задаёт её сам) |
| `OPENDATA_PG_USER` | `opendata` |
| `OPENDATA_PG_PASSWORD` | `opendata` |
