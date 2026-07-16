#!/bin/bash
# Publish to Maven Central Portal via REST API
set -e

USERNAME="2nyp7b"
echo -n "User Token Password: "
read -s TOKEN_PASS
echo ""

BASE="https://central.sonatype.com/api/v1/publisher"

publish() {
    local name="$1"
    local pom="$2"
    local jar="$3"
    local sources="$4"
    local javadoc="$5"

    echo "Publishing $name..."

    HTTP_CODE=$(curl -s -o /tmp/central-response.json -w "%{http_code}" \
        -u "$USERNAME:$TOKEN_PASS" \
        -F "pom=@$pom" \
        -F "jar=@$jar" \
        -F "sources=@$sources" \
        -F "javadoc=@$javadoc" \
        "$BASE/upload")

    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ] || [ "$HTTP_CODE" = "204" ]; then
        echo "  ✅ $name published"
    else
        echo "  ❌ $name failed (HTTP $HTTP_CODE)"
        cat /tmp/central-response.json
        echo ""
    fi
}

TARGET="sforce-api-core/target"
publish \
    "sforce-api-core" \
    "$TARGET/sforce-api-core-0.0.1.pom" \
    "$TARGET/sforce-api-core-0.0.1.jar" \
    "$TARGET/sforce-api-core-0.0.1-sources.jar" \
    "$TARGET/sforce-api-core-0.0.1-javadoc.jar"

TARGET="sforce-api-spring-boot-starter/target"
publish \
    "sforce-api-spring-boot-starter" \
    "$TARGET/sforce-api-spring-boot-starter-0.0.1.pom" \
    "$TARGET/sforce-api-spring-boot-starter-0.0.1.jar" \
    "$TARGET/sforce-api-spring-boot-starter-0.0.1-sources.jar" \
    "$TARGET/sforce-api-spring-boot-starter-0.0.1-javadoc.jar"

echo ""
echo "Done. Check https://central.sonatype.com/publishing for status."
