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

edit = service.edits().insert(packageName=PKG, body={}).execute()
eid = edit["id"]
print("EDIT", eid)

def safe(fn, label):
    try:
        r = fn().execute()
        print("==", label, "==")
        print(r)
    except Exception as ex:
        print("==", label, "ERR ==", type(ex).__name__, str(ex)[:300])

safe(lambda: service.edits().listings().list(packageName=PKG, editId=eid), "LISTINGS")
safe(lambda: service.edits().tracks().list(packageName=PKG, editId=eid), "TRACKS")
safe(lambda: service.edits().details().get(packageName=PKG, editId=eid), "DETAILS")
safe(lambda: service.edits().countries().list(packageName=PKG, editId=eid), "COUNTRIES")
service.edits().delete(packageName=PKG, editId=eid).execute()
