import json
from google.oauth2 import service_account
from googleapiclient.discovery import build

KEY = r"H:\psyc-app\play-creds\warmword-play-billing.json"
PKG = "com.charles.warmwords"
C = service_account.Credentials.from_service_account_file(KEY,
    scopes=["https://www.googleapis.com/auth/androidpublisher"])
S = build("androidpublisher", "v3", credentials=C)

# Base offers are auto-created per base plan; we just need to fetch/confirm them.
base = S.monetization().subscriptions().basePlans()
print("=== base plan offer resources ===")
try:
    r = base.offers().list(packageName=PKG, productId="warmword_premium",
                          basePlanId="monthly").execute()
    print("offers list:", json.dumps(r, indent=1))
except Exception as e:
    print("ERR", type(e).__name__, str(e)[:800])
