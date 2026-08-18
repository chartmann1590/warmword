import os, subprocess, json

OUT = r"H:\psyc-app\play-assets\video"
SHOTS = ["01-chat","02-journal","03-insights","04-findhelp","05-settings","06-paywall"]
SRC = r"H:\psyc-app\play-assets\screenshots\phone"
FONT = r"play-creds/font.ttf"
FF = r"C:\ProgramData\chocolatey\bin\ffmpeg.exe"
DUR = 31.56
N = len(SHOTS)
D = DUR / N
F = 0.6  # crossfade

# read captions
caps=[]
with open(os.path.join(OUT,"meta.txt")) as f:
    for line in f:
        if line.startswith("CAP "):
            _,rest=line[4:].split("|",1)
            caps.append(rest.strip())
assert len(caps)==N, (len(caps),N)

clips=[]
filters=[]
for i,sh in enumerate(SHOTS):
    img=os.path.join(SRC,sh+".png")
    lines=caps[i].split("\n")
    # pan/zoom + caption
    dt=""
    y0=1540
    for li,ln in enumerate(lines):
        yy=y0+li*66
        dt+=("drawtext=fontfile=%s:text='%s':fontcolor=white:fontsize=60:x=64:y=%d:shadowcolor=black:shadowx=3:shadowy=3:alpha=0.95,"%(FONT,ln.replace("'","''"),yy))
    filt=("[%d:v]scale=1170:2610:force_original_aspect_ratio=increase,crop=1080:1920:x='(1170-1080)*t/%f':y='(2610-1920)*t/%f',format=yuv420p,drawbox=x=0:y=1460:w=1080:h=460:color=black@0.45:t=460,%s[v%d]"%(i,D,D,dt[:-1],i))
    filters.append(filt)

# xfade concat
prev="v0"
xf=[]
for i in range(1,N):
    off = i*D - i*F  # cumulative offset
    xf.append("[%s][v%d]xfade=transition=fade:duration=%f:offset=%f[%s]"%(prev,i,F,off,"tmp%d"%(i) if i<N-1 else "vout"))
    prev="tmp%d"%(i)
xfilter=";"+";".join(xf)

cmd=[FF,"-y"]
# inputs: each image as looped video of duration D
for i,sh in enumerate(SHOTS):
    img=os.path.join(SRC,sh+".png")
    cmd+=["-loop","1","-t",str(D),"-i",img]
cmd+=["-filter_complex", ";".join(filters)+xfilter, "-map","[vout]","-c:v","libx264","-pix_fmt","yuv420p","-r","30","-preset","fast", os.path.join(OUT,"promo_video.mp4")]
print("running ffmpeg video...")
r=subprocess.run(cmd,capture_output=True,text=True,timeout=600)
print("RC",r.returncode)
print(r.stderr[-2000:] if r.returncode else "OK")
