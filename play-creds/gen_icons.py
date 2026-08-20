import os
from PIL import Image

SRC = r"H:\psyc-app\play-assets\graphics\icon_512.png"
ROOT = r"H:\psyc-app\app\src\main\res"

# Density -> launcher size (px) for legacy icon
DENS = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

img = Image.open(SRC).convert("RGBA")

for folder, size in DENS.items():
    d = os.path.join(ROOT, folder)
    os.makedirs(d, exist_ok=True)
    resized = img.resize((size, size), Image.LANCZOS)
    for name in ("ic_launcher.png", "ic_launcher_round.png"):
        resized.save(os.path.join(d, name))
        print("wrote", os.path.join(folder, name))
