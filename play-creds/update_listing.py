from google.oauth2 import service_account
from googleapiclient.discovery import build

KEY = r"H:\psyc-app\play-creds\warmword-play-billing.json"
PKG = "com.charles.warmwords"
C = service_account.Credentials.from_service_account_file(KEY,
    scopes=["https://www.googleapis.com/auth/androidpublisher"])
S = build("androidpublisher", "v3", credentials=C)

listing = {
  "title": "WarmWord: AI Mental Health",
  "shortDescription": "Private on-device AI companion & journal. Available in your language.",
  "fullDescription": (
    "WarmWord is a private, on-device AI mental health companion that listens "
    "without judgment. Chat whenever anxious, racing, or lonely thoughts feel "
    "too heavy to carry alone. The AI model runs entirely on your device, so "
    "your conversations stay yours.\n\n"
    "USE WARMWORD IN YOUR OWN LANGUAGE.\n"
    "WarmWord is now fully translated into the language you choose - every menu, "
    "button and screen, plus WarmWord's replies. Pick a language in on-boarding "
    "or Settings and a small AI translation model runs right on your phone, so "
    "the whole app appears in your language without anything leaving your device.\n\n"
    "Reflect and journal in a calm space designed to help you untangle the day. "
    "Gentle mood insights reveal quiet patterns over time, so you can notice "
    "what helps and what hurts. And if you ever need it, WarmWord surfaces "
    "crisis and helpline resources in seconds - no digging required.\n\n"
    "Make WarmWord uniquely yours by personalizing tones and reminders. "
    "Upgrade to Premium to unlock the full experience, including unlimited "
    "conversations and deeper insights.\n\n"
    "WarmWord is not a substitute for professional care. In an emergency, "
    "always contact your local emergency services."
  ),
}

e = S.edits().insert(packageName=PKG, body={}).execute()
eid = e["id"]
S.edits().listings().update(packageName=PKG, editId=eid, language="en-US", body=listing).execute()
print("en-US listing updated")

c = S.edits().commit(packageName=PKG, editId=eid).execute()
print("COMMITTED", c.get("id"))
