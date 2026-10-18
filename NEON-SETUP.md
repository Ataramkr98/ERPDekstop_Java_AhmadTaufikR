# Neon setup

Use JDK 21+ and PowerShell 7. Save the connection once:

```powershell
pwsh -NoProfile -File scripts/Configure-Neon.ps1
./run-desktop.cmd -Build
```

The connection is encrypted for the current Windows account in `%LOCALAPPDATA%/ERP-Desktop/neon.credential.xml`. It is never written into source or packaged artifacts. Runtime signing keys are generated in the same local profile. Alternatively supply `DATABASE_URL`, or `DB_JDBC_URL` plus `DB_USERNAME` and `DB_PASSWORD`, in the process environment. `run-backend.cmd -Build` starts only the loopback backend.

The URI adapter separates credentials from the JDBC URL and maps libpq's `channel_binding` to JDBC's `channelBinding`. A credential-free JDBC example is `jdbc:postgresql://your-endpoint-pooler.region.aws.neon.tech:5432/neondb?sslmode=require&channelBinding=require`. Ports are optional in PostgreSQL URIs; 5432 is the default. TLS and requested channel binding are retained.

Flyway applies pending migrations; no clean or schema reset is performed. Historical V14 creates the initial application account `admin` / `admin` in an empty installation. This is separate from the Neon database login. Change the application password from the account menu. Demo records are opt-in.

References: [pgJDBC connection properties](https://jdbc.postgresql.org/documentation/use/), [Neon connection errors](https://neon.com/docs/connect/connection-errors), [pgJDBC channel-binding fix](https://jdbc.postgresql.org/changelogs/2026-06-29-42.7.12-release/).
