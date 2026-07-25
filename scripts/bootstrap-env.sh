#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
env_file="${project_dir}/.env"

if [[ -e "${env_file}" ]]; then
  echo ".env already exists; leaving it unchanged."
  exit 0
fi

email="root@example.com"
password="Oo1!$(openssl rand -hex 18)"
auth_token="$(printf '%s:%s' "${email}" "${password}" | base64 | tr -d '\n')"

umask 077
{
  printf 'OPENOBSERVE_EMAIL=%s\n' "${email}"
  printf 'OPENOBSERVE_PASSWORD=%s\n' "${password}"
  printf 'OPENOBSERVE_AUTH_TOKEN=%s\n' "${auth_token}"
} > "${env_file}"

echo "Created .env with a random local-only OpenObserve password."
echo "OpenObserve email: ${email}"
echo "Retrieve the password with: docker compose config | sed -n '/ZO_ROOT_USER_PASSWORD/p'"
