import google.auth
from google.auth.transport.requests import Request
from google.oauth2 import service_account
from googleapiclient.discovery import build

SCOPES = ["https://www.googleapis.com/auth/androidpublisher"]
PKG = "com.charles.warmwords"

creds = service_account.Credentials.from_service_account_file(
    r"H:\psyc-app\play-creds\warmword-play-billing.json", scopes=SCOPES
)
creds.refresh(Request())
print("TOKEN OK, expires:", creds.expiry)

service = build("androidpublisher", "v3", credentials=creds)
try:
    try:
        app = service.apps().get(packageName=PKG).execute()
        print("APP EXISTS:", app.get("name"), app.get("appId"))
    except Exception as e2:
        print("APPS.GET ERROR:", type(e2).__name__, str(e2)[:400])
    try:
        edit = service.edits().insert(packageName=PKG, body={}).execute()
        print("EDIT INSERT OK, id=", edit.get("id"))
        service.edits().delete(packageName=PKG, editId=edit.get("id")).execute()
    except Exception as e3:
        print("EDIT INSERT ERROR:", type(e3).__name__, str(e3)[:400])
except Exception as e:
    print("APP GET ERROR:", type(e).__name__, str(e)[:500])
