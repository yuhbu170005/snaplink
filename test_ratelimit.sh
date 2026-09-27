#!/bin/bash
ENDPOINT="http://localhost:8080/api/urls"
echo "=========================================================="
echo "🎯 SNAPLINK RATE LIMITING LIVE TEST (DAYS 10-11)"
echo "Target Endpoint: $ENDPOINT (Guest limit: 10 req / 60s)"
echo "=========================================================="

for i in {1..14}; do
  echo -n "Request #$i -> "
  RESP=$(curl -i -s -X POST "$ENDPOINT" \
    -H "Content-Type: application/json" \
    -H "X-Forwarded-For: 203.0.113.$((100))" \
    -d "{\"originalUrl\": \"https://example.com/test-$i\"}")

  HTTP_CODE=$(echo "$RESP" | grep -i "^HTTP" | awk '{print $2}')
  LIMIT=$(echo "$RESP" | grep -i "X-RateLimit-Limit" | tr -d '\r' | awk '{print $2}')
  REMAINING=$(echo "$RESP" | grep -i "X-RateLimit-Remaining" | tr -d '\r' | awk '{print $2}')
  RESET=$(echo "$RESP" | grep -i "X-RateLimit-Reset" | tr -d '\r' | awk '{print $2}')
  RETRY=$(echo "$RESP" | grep -i "Retry-After" | tr -d '\r' | awk '{print $2}')

  echo "HTTP: $HTTP_CODE | Limit: $LIMIT | Remaining: $REMAINING | Reset: ${RESET}s | Retry-After: ${RETRY:-N/A}"
done
echo "=========================================================="
