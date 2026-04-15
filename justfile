default:
  @just --choose

clean:
  @docker compose down --volumes

up:
  @docker compose up -d

down:
  @docker compose down

build-docker:
  mvn spring-boot:build-image -DskipTests

benchmark-setup:
  @just down
  docker compose -f docker-compose-benchmark.yml up -d
  docker logs -f acme-allocationservice

benchmark:
  k6 run benchmark/allocation.js

benchmark-teardown:
  docker compose -f docker-compose-benchmark.yml down -v

run:
  mvn spring-boot:run -Dspring.liquibase.contexts=seed

demo:
  #!/usr/bin/env bash
  set -euo pipefail
  BODY=$(cat <<'EOF'
  {
    "employeeId": "123e4567-e89b-12d3-a456-426614174000",
    "policy": [
      {"equipmentType": "main_computer", "minConditionScore": 0.7, "preferredBrand": "Dell"},
      {"equipmentType": "monitor", "minConditionScore": 0.9},
      {"equipmentType": "monitor", "minConditionScore": 0.85}
    ]
  }
  EOF
  )
  ID=$(curl -sf -X POST "http://localhost:8080/allocations" \
    -H "Content-Type: application/json" \
    -d "$BODY" | jq -r '.allocationId')
  echo "Created allocation: $ID"
  while true; do
    RESP=$(curl -sf "http://localhost:8080/allocations/$ID")
    STATE=$(echo "$RESP" | jq -r '.state')
    echo "State: $STATE"
    case "$STATE" in
      allocated|confirmed|cancelled|failed) echo "$RESP" | jq .; break ;;
    esac
    sleep 1
  done

pdf:
  asciidoctor-pdf -r asciidoctor-diagram README.adoc
  asciidoctor-pdf -r asciidoctor-diagram ALGORITHM.adoc
  asciidoctor-pdf -r asciidoctor-diagram BENCHMARK.adoc

