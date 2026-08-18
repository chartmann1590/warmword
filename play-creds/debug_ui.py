import subprocess, xml.etree.ElementTree as ET
DEVICE="37220DLJG001ML"
ADB=r"C:\Users\Charles\AppData\Local\Android\Sdk\platform-tools\adb.exe"
def adb(a): return subprocess.run([ADB,"-s",DEVICE]+a,capture_output=True,text=True,timeout=60)
adb(["shell","input","keyevent","224"]); adb(["shell","input","keyevent","82"])
import time; time.sleep(1)
adb(["shell","uiautomator","dump","/sdcard/ui.xml"])
adb(["pull","/sdcard/ui.xml",r"C:\WINDOWS\TEMP\opencode\ui.xml"])
root=ET.parse(r"C:\WINDOWS\TEMP\opencode\ui.xml").getroot()
print("TOTAL NODES:", len(list(root.iter("node"))))
for n in root.iter("node"):
    t=n.get("text") or ""
    c=n.get("class") or ""
    cd=n.get("content-desc") or ""
    if t.strip() or cd.strip():
        print(repr(t[:40]), "|", c.split(".")[-1], "|", repr(cd[:30]))
