import os, re, subprocess, time, tempfile
from PIL import Image

DEVICE = "37220DLJG001ML"
PKG = "com.charles.warmwords"
OUT = r"H:\psyc-app\play-assets\screenshots\phone"
os.makedirs(OUT, exist_ok=True)
ADB = r"C:\Users\Charles\AppData\Local\Android\Sdk\platform-tools\adb.exe"

NODE_RE = re.compile(r"<node\b(.*?)/>", re.S)
ATTR_RE = re.compile(r'(\w+)="([^"]*)"')

def adb(a): return subprocess.run([ADB, "-s", DEVICE] + a, capture_output=True, text=True, timeout=60)
def wake():
    adb(["shell","input","keyevent","224"]); adb(["shell","input","keyevent","82"]); time.sleep(0.5)
def dump_raw():
    p = os.path.join(tempfile.gettempdir(), "ww_%d.xml" % int(time.time()*1000))
    wake(); adb(["shell","uiautomator","dump","/sdcard/ui.xml"]); adb(["pull","/sdcard/ui.xml",p])
    return open(p, encoding="utf-8", errors="ignore").read()
def parse_nodes(raw):
    out=[]
    for m in NODE_RE.finditer(raw):
        attrs=dict(ATTR_RE.findall(m.group(1)))
        b=attrs.get("bounds","")
        bm=re.findall(r"\d+", b)
        out.append({"text":attrs.get("text",""),"cd":attrs.get("content-desc",""),
                    "cls":attrs.get("class",""),"bounds":[int(x) for x in bm] if len(bm)==4 else None})
    return out
def find_node(nodes, target, substr=False, use_cd=False):
    tg=re.sub(r"\s+"," ",target).strip().lower()
    for n in nodes:
        t=re.sub(r"\s+"," ",(n["cd"] if use_cd else n["text"])).strip().lower()
        if not t: continue
        if substr:
            if tg in t: return n
        else:
            if t==tg: return n
    return None
def center(n):
    x1,y1,x2,y2=n["bounds"]; return (x1+x2)//2,(y1+y2)//2
def tap(n, wait=1.8):
    x,y=center(n); adb(["shell","input","tap",str(x),str(y)]); time.sleep(wait)
def tap_text(target, substr=False, wait=2.0):
    nodes=parse_nodes(dump_raw()); n=find_node(nodes,target,substr) or find_node(nodes,target,substr,True)
    if not n:
        print("  [tap] NOT FOUND:",target); return False
    tap(n,wait); return True
def tap_edit(substr, wait=0.6):
    nodes=parse_nodes(dump_raw())
    for n in nodes:
        if "android.widget.EditText" in n["cls"]:
            tap(n,wait); return True
    print("  [edit] NOT FOUND"); return False
def type_text(s): adb(["shell","input","text", s.replace(" ","%s").replace("'","")]); time.sleep(0.8)
def shot(name):
    p=os.path.join(OUT,name+".png")
    adb(["shell","screencap","-p","/sdcard/shot.png"]); adb(["pull","/sdcard/shot.png",p])
    try:
        im=Image.open(p).convert("RGB"); w,h=im.size; nw=1080; nh=int(round(h*nw/w)); im.resize((nw,nh)).save(p)
    except Exception as e: print("  resize err",e)
    print("  SHOT",name,os.path.getsize(p)); time.sleep(0.4)

print("=== launch ===")
adb(["shell","am","force-stop",PKG]); time.sleep(2)
adb(["shell","am","start","-n",PKG+"/.MainActivity"])
ready=False
for i in range(25):
    nodes=parse_nodes(dump_raw())
    if find_node(nodes,"Chat") and find_node(nodes,"Settings"):
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
