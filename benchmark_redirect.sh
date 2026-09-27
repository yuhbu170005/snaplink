#!/bin/bash
TARGET_URL="http://localhost:8080/torvalds"
echo "=========================================================="
echo "🎯 SNAPLINK REDIRECT LATENCY BENCHMARK (DAY 9)"
echo "Target Endpoint: $TARGET_URL"
echo "=========================================================="

echo -e "\n1. Test First Request (L2 Redis Hit / Cache Warmup):"
curl -o /dev/null -s -w "HTTP Status: %{http_code} | Total Time: %{time_total}s | DNS Time: %{time_namelookup}s | Connect Time: %{time_connect}s\n" "$TARGET_URL"

echo -e "\n2. Test Subsequent Requests (L1 In-Memory Caffeine Cache Hit):"
for i in {1..5}; do
  curl -o /dev/null -s -w "Req #$i -> HTTP: %{http_code} | Total Time: %{time_total}s (%{time_starttransfer}s)\n" "$TARGET_URL"
done

echo -e "\n3. Micro-benchmark (100 rapid requests to evaluate L1 Caffeine Cache throughput):"
python3 - << 'PYEOF'
import urllib.request
import time
import statistics

url = "http://localhost:8080/torvalds"
latencies = []

# Opener that doesn't follow redirects to measure pure redirect latency
class NoRedirectHandler(urllib.request.HTTPRedirectHandler):
    def http_error_302(self, req, fp, code, msg, headers):
        return fp

opener = urllib.request.build_opener(NoRedirectHandler)

for _ in range(100):
    t0 = time.perf_counter()
    try:
        resp = opener.open(url)
    except Exception as e:
        pass
    t1 = time.perf_counter()
    latencies.append((t1 - t0) * 1000.0) # in ms

print(f"Total Requests: {len(latencies)}")
print(f"Min Latency:    {min(latencies):.2f} ms")
print(f"Avg Latency:    {statistics.mean(latencies):.2f} ms")
print(f"Median (p50):   {statistics.median(latencies):.2f} ms")
latencies.sort()
p95 = latencies[int(len(latencies) * 0.95)]
p99 = latencies[int(len(latencies) * 0.99)]
print(f"p95 Latency:    {p95:.2f} ms")
print(f"p99 Latency:    {p99:.2f} ms")
print(f"Max Latency:    {max(latencies):.2f} ms")
PYEOF

echo "=========================================================="
