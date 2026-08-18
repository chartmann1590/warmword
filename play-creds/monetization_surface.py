import google.auth
from google.auth.transport.requests import Request
from google.oauth2 import service_account
from googleapiclient.discovery import build

SCOPES = ["https://www.googleapis.com/auth/androidpublisher"]
creds = service_account.Credentials.from_service_account_file(
    r"H:\psyc-app\play-creds\warmword-play-billing.json", scopes=SCOPES)
creds.refresh(Request())
service = build("androidpublisher", "v3", credentials=creds)

print("MONETIZATION:")
m = service.monetization()
for r in sorted(dir(m)):
    if not r.startswith("_") and callable(getattr(m, r)):
        try:
            sub = getattr(m, r)()
            methods = [x for x in sorted(dir(sub)) if not x.startswith("_") and callable(getattr(sub, x))]
            print("  monetization.%s -> %s" % (r, methods))
        except Exception as e:
            print("  monetization.%s -> ERR %s" % (r, e))
print("INAPPPRODUCTS:")
ip = service.inappproducts()
for r in sorted(dir(ip)):
    if not r.startswith("_") and callable(getattr(ip, r)):
        try:
            sub = getattr(ip, r)()
            methods = [x for x in sorted(dir(sub)) if not x.startswith("_") and callable(getattr(sub, x))]
            print("  inappproducts.%s -> %s" % (r, methods))
        except Exception as e:
            print("  inappproducts.%s -> ERR %s" % (r, e))
