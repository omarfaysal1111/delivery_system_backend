#!/bin/sh
set -e

# Render provides DATABASE_URL as postgres://user:pass@host:port/db
# Spring Boot needs jdbc:postgresql://user:pass@host:port/db
if [ -n "$DATABASE_URL" ] && [ -z "$DB_URL" ]; then
  DB_URL=$(echo "$DATABASE_URL" | sed 's|^postgres://|jdbc:postgresql://|')
  export DB_URL
fi

exec java ${JAVA_OPTS} -jar app.jar
