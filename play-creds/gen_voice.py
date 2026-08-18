import asyncio, edge_tts, subprocess, os, json

OUT = r"H:\psyc-app\play-assets\video"
os.makedirs(OUT, exist_ok=True)

SCRIPT = (
    "Meet WarmWord, your warm, private AI mental health companion. "
    "Chat whenever anxious thoughts feel too heavy to carry alone. "
    "Reflect and journal in a calm, judgment-free space. "
    "Watch gentle insights reveal quiet patterns in your mood. "
    "And if you ever need it, find help and crisis resources in seconds. "
    "Make WarmWord uniquely yours. "
    "Unlock Premium for the full experience. "
    "WarmWord. Calm, private, and always on your side."
)

VOICE = "en-US-AriaNeural"  # natural, warm female neural voice

async def main():
    comm = edge_tts.Communicate(SCRIPT, VOICE, rate="+3%")
    await comm.save(os.path.join(OUT, "voice.mp3"))
    print("voice saved")

asyncio.run(main())
# get duration
r = subprocess.run(["ffprobe","-v","error","-show_entries","format=duration",
                    "-of","json", os.path.join(OUT,"voice.mp3")],
                   capture_output=True, text=True)
dur = float(json.loads(r.stdout)["format"]["duration"])
print("DURATION", round(dur,2))
# split script into caption lines roughly aligned to 6 scenes
captions = [
    "Meet WarmWord -\nyour private AI companion",
    "Chat through\nanxious thoughts",
    "Journal in a\njudgment-free space",
    "Gentle insights\ninto your mood",
    "Find help and\ncrisis resources",
    "WarmWord Premium -\nunlock the full you",
]
# write captions + meta
with open(os.path.join(OUT,"meta.txt"),"w") as f:
    f.write(SCRIPT+"\n")
    f.write("DURATION %.2f\n"%dur)
    for i,c in enumerate(captions):
        f.write("CAP %d|%s\n"%(i,c))
print("captions written", len(captions))
