import os, json
from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload

KEY = r"H:\psyc-app\play-creds\warmword-play-billing.json"
PKG = "com.charles.warmwords"
CREDS = service_account.Credentials.from_service_account_file(KEY,
    scopes=["https://www.googleapis.com/auth/androidpublisher"])
S = build("androidpublisher","v3",credentials=CREDS)

A = S.edits().insert(packageName=PKG, body={}).execute()
EID = A["id"]
print("edit", EID)

SHOTS = ["01-chat","02-journal","03-insights","04-findhelp","05-settings","06-paywall"]
SDIR = r"H:\psyc-app\play-assets\screenshots\phone"
GDIR = r"H:\psyc-app\play-assets\graphics"

# 1. listings
listing = {
  "title": "WarmWord: AI Mental Health",
  "shortDescription": "Your private, on-device AI companion for calm, judgment-free support.",
  "fullDescription": (
    "WarmWord is a private, on-device AI mental health companion that listens "
    "without judgment. Chat whenever anxious, racing, or lonely thoughts feel "
    "too heavy to carry alone. The AI model runs entirely on your device, so "
    "your conversations stay yours.\n\n"
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
S.edits().listings().update(packageName=PKG, editId=EID, language="en-US", body=listing).execute()
print("listing set")

# 2. screenshots (phone)
for sh in SHOTS:
    p = os.path.join(SDIR, sh+".png")
    mb = MediaFileUpload(p, mimetype="image/png")
    r = S.edits().images().upload(packageName=PKG, editId=EID, language="en-US",
        imageType="phoneScreenshots", media_body=mb).execute()
    print("shot", sh, r.get("image",{}).get("id","?"))

# 3. feature graphic
fg = os.path.join(GDIR,"feature_graphic.png")
r = S.edits().images().upload(packageName=PKG, editId=EID, language="en-US",
    imageType="featureGraphic", media_body=MediaFileUpload(fg, mimetype="image/png")).execute()
print("feature", r.get("image",{}).get("id","?"))

# 4. icon
ic = os.path.join(GDIR,"icon_512.png")
r = S.edits().images().upload(packageName=PKG, editId=EID, language="en-US",
    imageType="icon", media_body=MediaFileUpload(ic, mimetype="image/png")).execute()
print("icon", r.get("image",{}).get("id","?"))

# 5. subscription (best-effort)
sub = {
  "packageName": PKG,
  "productId": "warmword_premium",
  "defaultBasePlanId": "monthly",
  "taxPolicy": "AUTO",
  "listings": [{"languageCode":"en-US","title":"WarmWord Premium",
                "description":"Unlock unlimited conversations and deeper mood insights."}],
  "basePlans": [{
     "basePlanId":"monthly","state":"ACTIVE","defaultPrice":None,
     "autoRenewingBasePlanType":{
        "billingPeriodDuration":"P1M","prorationMode":"CHARGE_PRORATED_PRICE",
        "gracePeriodDuration":"P3D","accountHoldDuration":"P7D","legacyCompatible":False,
        "autoRenewalType":"RENEW_EVERY_PERIOD"
     },
     "regionalConfigs":[{"regionCode":"US","price":{"currency":"USD","units":"9","nanos":990000000},
                         "newSubscriberCaptureRate":1.0}],
     "stateLastChangedTime":""
  }]
}
try:
    r = S.edits().subscriptions().create(packageName=PKG, editId=EID, body=sub).execute()
    print("subscription created", r.get("productId"))
except Exception as ex:
    print("SUBSCRIPTION ERROR:", repr(ex)[:500])

# commit
C = S.edits().commit(packageName=PKG, editId=EID).execute()
print("committed", C.get("id"))
