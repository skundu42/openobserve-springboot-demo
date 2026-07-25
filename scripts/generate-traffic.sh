#!/usr/bin/env bash
set -euo pipefail

base_url="${ORDER_SERVICE_URL:-http://localhost:8080}"
request_count="${1:-5}"

echo "Generating ${request_count} normal order traces..."
for request_number in $(seq 1 "${request_count}"); do
  curl --fail --silent --show-error \
    "${base_url}/api/orders/order-${request_number}?sku=OO-HOODIE&quantity=2"
  printf '\n'
done

echo "Generating one slow distributed trace..."
curl --fail --silent --show-error \
  "${base_url}/api/orders/order-slow?sku=SLOW-PACK&quantity=1"
printf '\n'

echo "Generating one expected business-error trace (HTTP 409)..."
http_status="$(
  curl --silent --show-error \
    --output /tmp/openobserve-demo-error-response.json \
    --write-out '%{http_code}' \
    "${base_url}/api/orders/order-rejected?sku=OUT-OF-STOCK&quantity=1"
)"
cat /tmp/openobserve-demo-error-response.json
printf '\nHTTP status: %s\n' "${http_status}"

if [[ "${http_status}" != "409" ]]; then
  echo "Expected HTTP 409 for the out-of-stock order." >&2
  exit 1
fi

echo "Traffic generation complete. Allow a few seconds for batch export."

