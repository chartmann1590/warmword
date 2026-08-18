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

print("TOP-LEVEL RESOURCES:")
for r in sorted(dir(service)):
    if not r.startswith("_"):
        print(" ", r)
print("\nEDITS SUB-RESOURCES:")
ed = service.edits()
for r in sorted(dir(ed)):
    if not r.startswith("_") and callable(getattr(ed, r)):
        try:
            sub = getattr(ed, r)()
            methods = [m for m in sorted(dir(sub)) if not m.startswith("_") and callable(getattr(sub, m))]
            print("  edits.%s -> %s" % (r, methods))
        except Exception as e:
            print("  edits.%s -> ERR %s" % (r, e))
