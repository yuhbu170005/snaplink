#!/bin/sh
set -eu

# Render provides a PostgreSQL URI. JDBC takes the host and database separately from credentials.
if [ -z "${DB_URL:-}" ] && [ -n "${RENDER_DATABASE_URL:-}" ]; then
    export DB_URL="jdbc:postgresql://${RENDER_DATABASE_URL##*@}"
fi

exec java $JAVA_OPTS -jar /app/app.jar
