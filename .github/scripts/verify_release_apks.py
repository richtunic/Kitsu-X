"""Check that release APKs retain all supported native architectures."""
import sys
import zipfile
from pathlib import Path

ARCHITECTURES = {
    "armeabi-v7a": (1, 40),
    "arm64-v8a": (2, 183),
    "x86": (1, 3),
    "x86_64": (2, 62),
}
REQUIRED_LIBRARIES = {"libmpv.so", "libplayer.so", "libavcodec.so", "libimagedecoder.so", "libsqlite3x.so"}


def verify(directory):
    for variant in (*ARCHITECTURES, "universal"):
        candidates = list(directory.glob(f"app-{variant}-release.apk")) + list(
            directory.glob(f"app-{variant}-release-unsigned.apk")
        )
        if len(candidates) != 1:
            raise ValueError(f"Expected one release APK for {variant}, found {len(candidates)}")
        expected = set(ARCHITECTURES) if variant == "universal" else {variant}
        with zipfile.ZipFile(candidates[0]) as apk:
            libraries = {}
            for name in apk.namelist():
                parts = name.split("/")
                if len(parts) != 3 or parts[0] != "lib" or not name.endswith(".so"):
                    continue
                abi = parts[1]
                if abi not in expected:
                    raise ValueError(f"Unexpected architecture {abi} in {variant}")
                header = apk.read(name)[:20]
                elf_class, machine = ARCHITECTURES[abi]
                if (header[:4] != b"\x7fELF" or header[4] != elf_class or header[5] != 1
                        or int.from_bytes(header[18:20], "little") != machine):
                    raise ValueError(f"Wrong ELF architecture in {variant}: {name}")
                libraries.setdefault(abi, set()).add(parts[2])
            if set(libraries) != expected:
                raise ValueError(f"Missing architecture in {variant}")
            for abi, names in libraries.items():
                missing = REQUIRED_LIBRARIES - names
                if missing:
                    raise ValueError(f"Missing required native libraries in {variant}/{abi}: {sorted(missing)}")
            print(f"{variant}: OK ({', '.join(f'{abi}: {len(names)} libraries' for abi, names in libraries.items())})")


if __name__ == "__main__":
    verify(Path(sys.argv[1]) if len(sys.argv) == 2 else Path("app/build/outputs/apk/release"))
