import sys
import re

def main():
    if len(sys.argv) < 2:
        print("Usage: python update_readme_downloads.py <tag_name>")
        sys.exit(1)

    tag = sys.argv[1]
    
    # Read README.md
    try:
        with open("README.md", "r", encoding="utf-8") as f:
            content = f.read()
    except FileNotFoundError:
        print("Error: README.md not found.")
        sys.exit(1)

    asset_prefix = f"Kitsu-X-{tag}"

    # Generate new downloads block for Spanish
    downloads_es = f"""<!-- START_DOWNLOADS_ES -->
### 📥 Descargas (Última versión: `{tag}`)

Para instalar Kitsu X, tu dispositivo debe contar con **Android 8.0 o superior**.

* **APK Universal**: [Descargar {asset_prefix}-universal.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/{asset_prefix}-universal.apk)
* **APK arm64-v8a**: [Descargar {asset_prefix}-arm64-v8a.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/{asset_prefix}-arm64-v8a.apk)
* **APK armeabi-v7a**: [Descargar {asset_prefix}-armeabi-v7a.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/{asset_prefix}-armeabi-v7a.apk)
* **APK x86**: [Descargar {asset_prefix}-x86.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/{asset_prefix}-x86.apk)
* **APK x86_64**: [Descargar {asset_prefix}-x86_64.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/{asset_prefix}-x86_64.apk)

*Para ver versiones anteriores o el historial completo de cambios, visita la sección de [Releases](https://github.com/richtunic/Kitsu-X/releases).*
<!-- END_DOWNLOADS_ES -->"""

    # Generate new downloads block for English
    downloads_en = f"""<!-- START_DOWNLOADS_EN -->
### 📥 Download (Latest version: `{tag}`)

To run Kitsu X, your device must have **Android 8.0 or higher**.

* **Universal APK**: [Download {asset_prefix}-universal.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/{asset_prefix}-universal.apk)
* **arm64-v8a APK**: [Download {asset_prefix}-arm64-v8a.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/{asset_prefix}-arm64-v8a.apk)
* **armeabi-v7a APK**: [Download {asset_prefix}-armeabi-v7a.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/{asset_prefix}-armeabi-v7a.apk)
* **x86 APK**: [Download {asset_prefix}-x86.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/{asset_prefix}-x86.apk)
* **x86_64 APK**: [Download {asset_prefix}-x86_64.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/{asset_prefix}-x86_64.apk)

*To see older versions or the changelog, check the [Releases](https://github.com/richtunic/Kitsu-X/releases) page.*
<!-- END_DOWNLOADS_EN -->"""

    # Replace in content using regex with DOTALL flag to match multiline blocks
    new_content = re.sub(
        r"<!-- START_DOWNLOADS_ES -->.*?<!-- END_DOWNLOADS_ES -->",
        downloads_es,
        content,
        flags=re.DOTALL
    )

    new_content = re.sub(
        r"<!-- START_DOWNLOADS_EN -->.*?<!-- END_DOWNLOADS_EN -->",
        downloads_en,
        new_content,
        flags=re.DOTALL
    )

    if new_content == content:
        print("Warning: No placeholders matched or replacement produced identical content.")
    
    # Write back README.md
    with open("README.md", "w", encoding="utf-8") as f:
        f.write(new_content)

    print(f"Successfully updated README.md downloads section to tag {tag}")

if __name__ == "__main__":
    main()
