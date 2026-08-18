import os, glob
from PIL import Image, ImageDraw, ImageFilter, ImageFont

OUT = r"H:\psyc-app\play-assets\graphics"
os.makedirs(OUT, exist_ok=True)

def font(size, bold=True):
    names = ["arialbd.ttf","arial.ttf","segoeui.ttf","trebucbd.ttf","CALIBRIB.TTF"]
    for n in names:
        for base in [r"C:\Windows\Fonts", r"C:\Windows\Fonts\en-us"]:
            p = os.path.join(base, n)
            if os.path.exists(p):
                try: return ImageFont.truetype(p, size)
                except: pass
    return ImageFont.load_default()

ORANGE = (255,140,66)
TEAL   = (77,182,172)
LAV    = (225,190,231)
CREAM  = (251,245,237)
WHITE  = (255,255,255)
DARK   = (33,33,33)

# ---------- Feature graphic 1024x500 ----------
W,H = 1024,500
img = Image.new("RGB",(W,H),CREAM)
px = img.load()
# diagonal gradient orange -> teal
for y in range(H):
    for x in range(W):
        t = (x/W*0.6 + y/H*0.4)
        r = int(ORANGE[0]+(TEAL[0]-ORANGE[0])*t)
        g = int(ORANGE[1]+(TEAL[1]-ORANGE[1])*t)
        b = int(ORANGE[2]+(TEAL[2]-ORANGE[2])*t)
        px[x,y]=(r,g,b)
# soft glow blobs
blob = Image.new("RGBA",(W,H),(0,0,0,0)); bd=ImageDraw.Draw(blob)
bd.ellipse([620,120,1000,500], fill=(LAV[0],LAV[1],LAV[2],90))
bd.ellipse([40,-120,420,260], fill=(WHITE[0],WHITE[1],WHITE[2],60))
blob = blob.filter(ImageFilter.GaussianBlur(40))
img = Image.alpha_composite(img.convert("RGBA"), blob).convert("RGB")
d = ImageDraw.Draw(img)
# rounded card behind text
d.rounded_rectangle([60,120,560,380], radius=28, fill=(255,255,255,220))
d.text((92,150), "WarmWord", font=font(72), fill=ORANGE)
d.text((92,245), "Your AI mental", font=font(40), fill=DARK)
d.text((92,295), "health companion", font=font(40), fill=DARK)
d.text((92,350), "Calm, private, on-device.", font=font(26), fill=(117,117,117))
img.save(os.path.join(OUT,"feature_graphic.png"))
print("feature_graphic.png", os.path.getsize(os.path.join(OUT,"feature_graphic.png")))

# ---------- Icon 512x512 ----------
S=512
icon = Image.new("RGBA",(S,S),(0,0,0,0))
idr = ImageDraw.Draw(icon)
# rounded square bg gradient orange
for y in range(S):
    for x in range(S):
        t=y/S
        r=int(ORANGE[0]+(TEAL[0]-ORANGE[0])*t); g=int(ORANGE[1]+(TEAL[1]-ORANGE[1])*t); b=int(ORANGE[2]+(TEAL[2]-ORANGE[2])*t)
        icon.load()[x,y]=(r,g,b,255)
icon = icon.filter(ImageFilter.GaussianBlur(0))
mask = Image.new("L",(S,S),0); ImageDraw.Draw(mask).rounded_rectangle([0,0,S,S], radius=110, fill=255)
icon.putalpha(mask)
d=ImageDraw.Draw(icon)
# white speech bubble
bx,by,bw,bh = 126,120,260,210
d.rounded_rectangle([bx,by,bx+bw,by+bh], radius=46, fill=WHITE)
d.polygon([(bx+70,by+bh),(bx+70,by+bh+46),(bx+128,by+bh)], fill=WHITE)
# "W" in orange
d.text((S/2-58, by+40), "W", font=font(150), fill=ORANGE)
icon.save(os.path.join(OUT,"icon_512.png"))
print("icon saved", os.path.getsize(os.path.join(OUT,"icon_512.png")))
