import re
import sys
from pathlib import Path


def downloads(tag, language):
    prefix = f"Kitsu-X-{tag}"
    base = f"https://github.com/richtunic/Kitsu-X/releases/download/{tag}"
    spanish = language == "ES"
    title = f"Descargas (Versión: `{tag}`)" if spanish else f"Download (Version: `{tag}`)"
    requirement = (
        "Requiere **Android 8.0 o superior**. Elige el APK según la arquitectura de tu sistema Android; "
        "si no la conoces, utiliza el universal."
        if spanish else
        "Requires **Android 8.0 or higher**. Choose the APK for your Android system architecture; "
        "if unsure, use the universal APK."
    )
    architectures = [
        ("arm64-v8a", "ARM de 64 bits" if spanish else "64-bit ARM"),
        ("armeabi-v7a", "ARM de 32 bits" if spanish else "32-bit ARM"),
        ("x86_64", "Intel/AMD de 64 bits" if spanish else "64-bit Intel/AMD"),
        ("x86", "Intel/AMD de 32 bits" if spanish else "32-bit Intel/AMD"),
        ("universal", "Todas las arquitecturas (archivo más grande)" if spanish else "All architectures (larger file)"),
    ]
    lines = [f"<!-- START_DOWNLOADS_{language} -->", f"### 📥 {title}", "", requirement, ""]
    for abi, label in architectures:
        lines.append(f"* **{label} ({abi})**: [APK]({base}/{prefix}-{abi}.apk)")
    lines.extend(["", f"[SHA-256]({base}/SHA256SUMS.txt)", "", "[Releases](https://github.com/richtunic/Kitsu-X/releases)", f"<!-- END_DOWNLOADS_{language} -->"])
    return "\n".join(lines)


def main():
    if len(sys.argv) != 2 or not re.fullmatch(r"v\d+\.\d+\.\d+", sys.argv[1]):
        raise SystemExit("Usage: python update_readme_downloads.py vX.Y.Z")
    path = Path("README.md")
    content = path.read_text(encoding="utf-8")
    for language in ("ES", "EN"):
        content, count = re.subn(
            rf"<!-- START_DOWNLOADS_{language} -->.*?<!-- END_DOWNLOADS_{language} -->",
            lambda _: downloads(sys.argv[1], language),
            content,
            flags=re.DOTALL,
        )
        if count != 1:
            raise SystemExit(f"Expected exactly one {language} downloads block")
    path.write_text(content, encoding="utf-8")
    print(f"Updated README downloads for {sys.argv[1]}")


if __name__ == "__main__":
    main()
