#!/bin/sh
set -e

# Render provides DATABASE_URL as postgres://user:pass@host:port/db
# Spring Boot needs jdbc:postgresql://... with credentials as separate properties
if [ -n "$DATABASE_URL" ] && [ -z "$DB_URL" ]; then
  DB_URL=$(echo "$DATABASE_URL" | sed 's|^postgres://|jdbc:postgresql://|')
  export DB_URL

  # Extract credentials so HikariCP uses them (it ignores embedded URL credentials)
  if [ -z "$DB_USERNAME" ]; then
    DB_USERNAME=$(echo "$DATABASE_URL" | sed 's|^postgres://\([^:]*\):.*|\1|')
    export DB_USERNAME
  fi
  if [ -z "$DB_PASSWORD" ]; then
    DB_PASSWORD=$(echo "$DATABASE_URL" | sed 's|^postgres://[^:]*:\([^@]*\)@.*|\1|')
    export DB_PASSWORD
  fi
fi

exec java ${JAVA_OPTS} -jar app.jar
