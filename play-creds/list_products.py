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

print("=== existing subscriptions ===")
try:
    subs = service.monetization().subscriptions().list(packageName=PKG).execute()
    print(subs)
except Exception as e:
    print("ERR", type(e).__name__, str(e)[:300])

print("=== existing inappproducts ===")
try:
    ip = service.inappproducts().list(packageName=PKG).execute()
    print(ip)
except Exception as e:
    print("ERR", type(e).__name__, str(e)[:300])

print("=== app details ===")
edit = service.edits().insert(packageName=PKG, body={}).execute()
eid = edit["id"]
try:
    d = service.edits().details().get(packageName=PKG, editId=eid).execute()
    print(d)
except Exception as e:
    print("ERR", type(e).__name__, str(e)[:300])
service.edits().delete(packageName=PKG, editId=eid).execute()
