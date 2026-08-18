import os, re, subprocess, time, xml.etree.ElementTree as ET
from PIL import Image

DEVICE = "37220DLJG001ML"
PKG = "com.charles.warmwords.app"
OUT = r"H:\psyc-app\play-assets\screenshots\phone"
os.makedirs(OUT, exist_ok=True)
ADB = r"C:\Users\Charles\AppData\Local\Android\Sdk\platform-tools\adb.exe"
UI = r"C:\WINDOWS\TEMP\opencode\ui.xml"

def adb(a): return subprocess.run([ADB, "-s", DEVICE] + a, capture_output=True, text=True, timeout=60)
def wake():
    adb(["shell","input","keyevent","224"]); adb(["shell","input","keyevent","82"]); time.sleep(0.5)
def dump():
    import tempfile, os as _os
    p = _os.path.join(tempfile.gettempdir(), "ww_ui_%d.xml" % int(time.time()*1000))
    wake(); adb(["shell","uiautomator","dump","/sdcard/ui.xml"]); adb(["pull","/sdcard/ui.xml",p])
    return ET.parse(p).getroot()
def find(r, text, cls=None, substr=False, cd=False):
    for n in r.iter("node"):
        t = ((n.get("content-desc") if cd else n.get("text")) or "").strip()
        c = n.get("class") or ""
        ok = True
        if text is not None:
            ok = ok and ((text.lower() in t.lower()) if substr else (t.lower()==text.lower()))
        if cls is not None: ok = ok and (cls in c)
        if ok: return n
    return None
def center(n):
    x1,y1,x2,y2 = map(int, re.findall(r"\d+", n.get("bounds"))); return (x1+x2)//2,(y1+y2)//2
def tap(n, wait=1.5):
    x,y=center(n); adb(["shell","input","tap",str(x),str(y)]); time.sleep(wait)
def tap_text(text, substr=False, wait=2.0):
    r=dump(); n=find(r,text,None,substr) or find(r,text,None,substr,True)
    if not n: print("  [tap] NOT FOUND:",text); return False
    tap(n,wait); return True
def shot(name):
    p=os.path.join(OUT,name+".png")
    adb(["shell","screencap","-p","/sdcard/shot.png"]); adb(["pull","/sdcard/shot.png",p])
    try:
        im=Image.open(p).convert("RGB"); w,h=im.size; nw=1080; nh=int(round(h*nw/w)); im.resize((nw,nh)).save(p)
    except Exception as e: print("  resize err",e)
    print("  SHOT",name,os.path.getsize(p)); time.sleep(0.4)

print("=== wait for main app ===")
adb(["shell","am","force-stop",PKG]); time.sleep(2)
adb(["shell","am","start","-n",PKG+"/.MainActivity"])
ready=False
for i in range(20):
    r=dump()
    txts=[(n.get("text") or "") for n in r.iter("node")]
    chat=find(r,"Chat"); sett=find(r,"Settings")
    print("  poll",i,"nodes",len(list(r.iter("node"))),"chat",bool(chat),"sett",bool(sett))
    if chat and sett:
        ready=True; print("  ready after",i); break
    time.sleep(1.5)
print("  ready=",ready)

print("=== 01 chat ==="); shot("01-chat")
print("=== 02 journal ==="); tap_text("Journal",wait=2.5); shot("02-journal")
print("=== 03 insights ==="); tap_text("Chat",wait=1); tap_text("Insights",wait=2.5); shot("03-insights")
print("=== 04 findhelp ==="); tap_text("Chat",wait=1); tap_text("Find Help",wait=2.5); shot("04-findhelp")
print("=== 05 settings ==="); tap_text("Chat",wait=1); tap_text("Settings",wait=2.5); shot("05-settings")
print("=== 06 paywall ==="); tap_text("Upgrade to Premium",wait=3); shot("06-paywall")
print("=== DONE ===")
