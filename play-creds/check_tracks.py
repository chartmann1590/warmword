import json
from google.oauth2 import service_account
from googleapiclient.discovery import build

KEY = r"H:\psyc-app\play-creds\warmword-play-billing.json"
PKG = "com.charles.warmwords"
C = service_account.Credentials.from_service_account_file(KEY, scopes=["https://www.googleapis.com/auth/androidpublisher"])
S = build("androidpublisher", "v3", credentials=C)

edit = S.edits().insert(packageName=PKG, body={}).execute()
eid = edit["id"]
for track in ["internal", "alpha", "beta", "production"]:
    try:
        t = S.edits().tracks().get(packageName=PKG, editId=eid, track=track).execute()
        print("=" * 6, track, "=" * 6)
        print(json.dumps(t, indent=1)[:2000])
    except Exception as e:
        print(track, "ERR", str(e)[:200])
try:
    S.edits().delete(packageName=PKG, editId=eid).execute()
except Exception:
    pass

print("\n=== internal app sharing / testers (tracks in internal app sharing via api not available) ===")
# Try to read the internal testing config via subscriptions (not available) - skip
