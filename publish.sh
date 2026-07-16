#!/bin/bash
set -e

USERNAME="2nyp7b"
echo -n "User Token Password: "
read -s TOKEN_PASS
echo ""

BEARER=$(echo -n "$USERNAME:$TOKEN_PASS" | base64)

# Build ZIP with Maven repository layout
echo "Creating bundle..."
BUNDLE="/tmp/central-bundle.zip"
WORK="/tmp/central-bundle-work"
rm -f "$BUNDLE"
rm -rf "$WORK"

NS="io/github/phper666"
VERSION="0.0.1"

# Helper to copy artifacts and generate signatures + checksums
copy_and_sign() {
    local module=$1
    local artifact=$2
    local dir="$WORK/$NS/$artifact/$VERSION"
    mkdir -p "$dir"

    cp "$module/.flattened-pom.xml" "$dir/$artifact-$VERSION.pom"
    cp "$module/target/$artifact-$VERSION.jar" "$dir/"
    cp "$module/target/$artifact-$VERSION-sources.jar" "$dir/"
    cp "$module/target/$artifact-$VERSION-javadoc.jar" "$dir/"

    cd "$dir"

    # Sign every artifact file
    for f in *.pom *.jar; do
        gpg --batch --pinentry-mode loopback --passphrase "sforce@2024" -ab "$f"
    done

    # Generate checksums for artifacts and their signatures
    for f in *.pom *.jar *.asc; do
        if command -v md5sum >/dev/null 2>&1; then
            md5sum "$f" | cut -d' ' -f1 > "$f.md5"
        else
            md5 -r "$f" | cut -d' ' -f1 > "$f.md5"
        fi

        if command -v sha1sum >/dev/null 2>&1; then
            sha1sum "$f" | cut -d' ' -f1 > "$f.sha1"
        else
            shasum -a 1 "$f" | cut -d' ' -f1 > "$f.sha1"
        fi
    done

    cd - >/dev/null
}

copy_and_sign "sforce-api-core" "sforce-api-core"
copy_and_sign "sforce-api-spring-boot-starter" "sforce-api-spring-boot-starter"

cd "$WORK"
zip -q -r "$BUNDLE" .
cd - >/dev/null

echo "Bundle size: $(du -h "$BUNDLE" | cut -f1)"
echo ""
echo "Uploading to Maven Central..."

HTTP_CODE=$(curl -s -o /tmp/central-response.json -w "%{http_code}" \
  --connect-timeout 30 --max-time 120 \
  -H "Authorization: Bearer $BEARER" \
  -F "bundle=@$BUNDLE" \
  "https://central.sonatype.com/api/v1/publisher/upload?publishingType=AUTOMATIC")

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ] || [ "$HTTP_CODE" = "204" ]; then
  echo "✅ Upload successful!"
  cat /tmp/central-response.json
elif [ "$HTTP_CODE" = "401" ]; then
  echo "❌ Unauthorized - 密码错误"
else
  echo "❌ Failed (HTTP $HTTP_CODE)"
  cat /tmp/central-response.json
fi

rm -rf "$WORK" "$BUNDLE"
