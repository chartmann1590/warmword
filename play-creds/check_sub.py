import google.auth
from google.auth.transport.requests import Request
from google.oauth2 import service_account
from googleapiclient.discovery import build

SCOPES = ["https://www.googleapis.com/auth/androidpublisher"]
PKG = "com.charles.warmwords"
creds = service_account.Credentials.from_service_account_file(
    r"H:\psyc-app\play-creds\warmword-play-billing.json", scopes=SCOPES)
creds.refresh(Request())
service = build("androidpublisher", "v3", credentials=creds)

print("=== subscription detail ===")
try:
    sub = service.monetization().subscriptions().get(
        packageName=PKG, productId="warmword_premium").execute()
    import json
    print(json.dumps(sub, indent=1)[:4000])
except Exception as e:
    print("ERR", type(e).__name__, str(e)[:400])

print("=== tracks ===")
try:
    tracks = service.edits().tracks().list(packageName=PKG, editId='__fake__').execute()
except TypeError:
    pass
except Exception:
    pass
try:
    edit = service.edits().insert(packageName=PKG, body={}).execute()
    eid = edit["id"]
    for track in ["production", "internal", "alpha", "beta"]:
        try:
            t = service.edits().tracks().get(packageName=PKG, editId=eid, track=track).execute()
            print(track, "->", [(r.get("versionCodes"), r.get("status")) for r in t.get("releases", [])])
        except Exception as e:
            print(track, "ERR", str(e)[:200])
    service.edits().delete(packageName=PKG, editId=eid).execute()
except Exception as e:
    print("EDIT ERR", type(e).__name__, str(e)[:300])