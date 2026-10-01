# PostgreSQL

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

Init-скрипт (`docker/postgres/init/01-opendata.sql`) создаёт:

- `opendata_test` — контрольную таблицу из ТЗ, разделы 19 и 34;
- `users` и `orders` с PK, FK, UNIQUE, CHECK, индексом, триггером, JSONB и BYTEA для проверки Explorer, JSON и LOB;
- `user_order_totals` (view), `touch_created_at()` (function), `opendata_seq` (sequence), `order_status` (enum type);
- `big_table` на 200 000 строк для проверки paging, fetch more и cancel.

Сброс стенда: `docker compose -f docker\docker-compose.yml down -v`.

## Data Source в IDE

Data Source создаётся штатными средствами Database Tools (ТЗ, раздел 11):
**Database** → **+** → **Data Source** → **PostgreSQL**. Укажите Host, Port, Database, User и Password
из таблицы выше. Если появится предложение **Download missing driver files**, скачайте драйвер штатным
Driver Manager. **Test Connection** → **OK**.

Пароль сохраняется штатным механизмом Database Tools (IntelliJ `PasswordSafe`). Собственный
`DataSourceConfig` не нужен.

### Без доступа в интернет

Driver Manager не сможет скачать драйвер. Положите `postgresql-42.x.jar` (Maven Central,
`org.postgresql:postgresql`) локально и укажите его в **Drivers → PostgreSQL → Driver Files → + Custom JARs**.

## Ошибки (ТЗ, раздел 26)

PostgreSQL возвращает в `ServerErrorMessage` поля ERROR, SQLSTATE, Position, Detail, Hint и Constraint.
Штатная SQL Console Database Tools должна показывать их в окне вывода и подсвечивать позицию ошибки в редакторе; это проверяется пунктом 2.5 в `docs/acceptance.md`.
Тест `PostgresJdbcSmokeTest.sqlErrorHasStateAndPosition` подтверждает, что стенд их возвращает.

Пример для ручной проверки: `INSERT INTO orders(user_id, amount) VALUES (999, -1);`. Ожидается нарушение
CHECK или FK с заполненным полем Constraint.

## Переменные для тестов

| Переменная | По умолчанию |
|---|---|
| `OPENDATA_PG_URL` | не задана: JDBC-тесты пропускаются (`test.ps1 -WithPostgres` задаёт её сам) |
| `OPENDATA_PG_USER` | `opendata` |
| `OPENDATA_PG_PASSWORD` | `opendata` |
