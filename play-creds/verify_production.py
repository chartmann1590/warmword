import json
from google.oauth2 import service_account
from googleapiclient.discovery import build

KEY = r"H:\psyc-app\play-creds\warmword-play-billing.json"
PKG = "com.charles.warmwords"
C = service_account.Credentials.from_service_account_file(KEY,
    scopes=["https://www.googleapis.com/auth/androidpublisher"])
S = build("androidpublisher", "v3", credentials=C)

e = S.edits().insert(packageName=PKG, body={}).execute()
eid = e["id"]
try:
    t = S.edits().tracks().get(packageName=PKG, editId=eid, track="production").execute()
    print("=== production track ===")
    print(json.dumps(t, indent=1))
except Exception as ex:
    print("ERR", str(ex)[:300])
try:
    S.edits().delete(packageName=PKG, editId=eid).execute()
except Exception:
    pass
