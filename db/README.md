# Bank99 — PostgreSQL setup

The Android app connects **directly** to PostgreSQL via JDBC. PostgreSQL must be running and seeded *before* you launch the app on the emulator.

## 1. Install PostgreSQL 18 (Windows)

Download the installer from <https://www.postgresql.org/download/windows/>. Accept defaults; keep the `postgres` superuser password somewhere safe.

Tested on PostgreSQL 18. The schema and seed scripts only use features available since PG10 (`BIGSERIAL`, `ON CONFLICT`, `RETURNING`, `TRUNCATE ... RESTART IDENTITY`), so they also run on 16 / 17 without changes.

## 2. Create the database and user

Open `SQL Shell (psql)` as the `postgres` superuser, or run from a regular terminal:

```bash
psql -U postgres -h localhost
```

In `psql`:

```sql
CREATE USER bank99user WITH PASSWORD 'bank99pass';
CREATE DATABASE bank99 OWNER bank99user;
\c bank99
GRANT ALL ON SCHEMA public TO bank99user;
\q
```

## 3. Load schema + seed

From the project root (`c:\DDrive\GuruBank99`):

```bash
psql -U bank99user -h localhost -d bank99 -f db/schema.sql
psql -U bank99user -h localhost -d bank99 -f db/seed.sql
```

Verify:

```bash
psql -U bank99user -h localhost -d bank99 -c "SELECT username, status FROM users;"
```

You should see `C10001 / ACTIVE`, `C10002 / ACTIVE`, `C10003 / LOCKED`.

## 4. Allow the Android emulator to connect

The Android emulator reaches the host machine at the special alias **`10.0.2.2`**. The app's JDBC URL is already configured to:

```
jdbc:postgresql://10.0.2.2:5432/bank99
```

You usually do not need to touch `pg_hba.conf` for this — `10.0.2.2` arrives at PostgreSQL as `127.0.0.1` because it's a NAT'd loopback. If you see "no pg_hba.conf entry" errors, locate `pg_hba.conf` (e.g. `C:\Program Files\PostgreSQL\18\data\pg_hba.conf`) and ensure this line exists:

```
host    bank99    bank99user    127.0.0.1/32    scram-sha-256
```

Then restart the PostgreSQL Windows service.

## 5. Android API level requirement

The app sets `minSdk 26` (Android 8.0 Oreo) because the PostgreSQL JDBC driver (42.7.x) ships bytecode that uses `MethodHandle.invoke` in its optional Kerberos/GSS and SecurityManager code paths — D8 only accepts those instructions from API 26 onwards. We never exercise those code paths at runtime (the app uses `scram-sha-256` auth and Android does not install a SecurityManager), but D8 still has to dex the classes.

If you really need to run on API 24/25, the alternatives are:
- Use a non-JDBC client (Room/SQLite, or build a thin REST backend).
- Bundle a stripped pgjdbc that removes `org.postgresql.gss.*` and the SecurityManager helpers (manual repack — not recommended).

## 6. Real device (LAN access)

When running on a phone instead of the emulator the `10.0.2.2` shortcut does NOT work — the device sees that as a public IP and the connection times out. Use your dev machine's actual LAN IP instead.

`app/build.gradle` is already set to `jdbc:postgresql://192.168.100.86:5432/bank99`. If your `ipconfig` IPv4 changes (router reboot, new DHCP lease, etc.), update that value and rebuild.

For the one-time Postgres + firewall setup, run:

```powershell
# Right-click PowerShell → Run as administrator
cd C:\DDrive\GuruBank99
powershell -ExecutionPolicy Bypass -File db\setup-lan-access.ps1
```

The script:
- Backs up `pg_hba.conf`, appends `host bank99 bank99user 192.168.100.0/24 scram-sha-256`.
- Reloads PostgreSQL (`pg_ctl reload` — no restart needed).
- Adds a Windows Firewall inbound rule for TCP/5432 limited to that subnet.

It is idempotent — re-running is safe. Pass `-Subnet`, `-HbaPath`, etc. to override defaults.

After the script finishes, sanity-check from another machine (or just back on your dev box):

```powershell
psql -h 192.168.100.86 -U bank99user -d bank99 -c "SELECT username FROM users;"
```

If that works, the Android phone will too.

## 7. Re-seeding during development

`db/seed.sql` runs `TRUNCATE`/`DELETE` first, so it is safe to re-run any time you want to reset to the canonical fake data.

## 8. Troubleshooting

### `ERROR: permission denied for table users`

Triggered when `schema.sql` was loaded by the `postgres` superuser instead of `bank99user` — tables end up owned by `postgres` and PG15+ no longer auto-grants `public` schema permissions to other users.

Fix without losing your seed data: run the prepared one-shot grants file as the postgres superuser (you'll be prompted for the postgres install password):

```powershell
& 'C:\Program Files\PostgreSQL\18\bin\psql.exe' -h 127.0.0.1 -U postgres -d bank99 -f db\grant-permissions.sql
```

Then retry from the app — login should now succeed.

The current `db/schema.sql` already includes the same `GRANT` block at the bottom, so future re-runs won't hit this.

### App still times out after the fix

Make sure the emulator is running and connected to the network. If you switched from a real device back to the emulator, confirm `app/build.gradle` has `DB_URL = jdbc:postgresql://10.0.2.2:5432/bank99` (the emulator host alias), then `gradlew assembleDebug` and reinstall.

## 9. Test credentials

| Username | Password | State |
|----------|----------|-------|
| C10001   | Password1! | Active — owns 9900000001 (SAVING 5,000,000) + 9900000002 (CURRENT 2,000,000) |
| C10002   | Password1! | Active — owns 9900000003 (CURRENT 500,000) |
| C10003   | Password1! | **Locked** for 15 minutes — use this to exercise the F50 lockout path |
