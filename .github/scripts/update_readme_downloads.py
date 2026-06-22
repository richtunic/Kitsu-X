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

    # Generate new downloads block for Spanish
    downloads_es = f"""<!-- START_DOWNLOADS_ES -->
### 📥 Descargas (Última versión: `{tag}`)

Para instalar Kitsu X, tu dispositivo debe contar con **Android 8.0 o superior**.

* **APK Universal**: [Descargar app-universal-release.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/app-universal-release.apk)
* **APK arm64-v8a**: [Descargar app-arm64-v8a-release.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/app-arm64-v8a-release.apk)
* **APK armeabi-v7a**: [Descargar app-armeabi-v7a-release.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/app-armeabi-v7a-release.apk)
* **APK x86**: [Descargar app-x86-release.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/app-x86-release.apk)
* **APK x86_64**: [Descargar app-x86_64-release.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/app-x86_64-release.apk)

*Para ver versiones anteriores o el historial completo de cambios, visita la sección de [Releases](https://github.com/richtunic/Kitsu-X/releases).*
<!-- END_DOWNLOADS_ES -->"""

    # Generate new downloads block for English
    downloads_en = f"""<!-- START_DOWNLOADS_EN -->
### 📥 Download (Latest version: `{tag}`)

To run Kitsu X, your device must have **Android 8.0 or higher**.

* **Universal APK**: [Download app-universal-release.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/app-universal-release.apk)
* **arm64-v8a APK**: [Download app-arm64-v8a-release.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/app-arm64-v8a-release.apk)
* **armeabi-v7a APK**: [Download app-armeabi-v7a-release.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/app-armeabi-v7a-release.apk)
* **x86 APK**: [Download app-x86-release.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/app-x86-release.apk)
* **x86_64 APK**: [Download app-x86_64-release.apk](https://github.com/richtunic/Kitsu-X/releases/download/{tag}/app-x86_64-release.apk)

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
