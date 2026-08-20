import json
from google.oauth2 import service_account
from googleapiclient.discovery import build

KEY = r"H:\psyc-app\play-creds\warmword-play-billing.json"
PKG = "com.charles.warmwords"
C = service_account.Credentials.from_service_account_file(KEY, scopes=["https://www.googleapis.com/auth/androidpublisher"])
S = build("androidpublisher", "v3", credentials=C)

sub = S.monetization().subscriptions().get(packageName=PKG, productId="warmword_premium").execute()
print("WARMWORD_premium keys:", list(sub.keys()))
for bp in sub.get("basePlans", []):
    print("basePlan", bp.get("basePlanId"), "state", bp.get("state"))
    print("  suboffer keys:", [k for k in bp.keys()])
    offs = bp.get("subscriptionOffers") or bp.get("otherRecurringBasePlanTypes") or bp.get("offers")
    print("  offers field:", json.dumps(offs)[:800])

# proper list with pagination params
print("\n=== offers().list raw ===")
r = S.monetization().subscriptions().basePlans().offers().list(
    packageName=PKG, productId="warmword_premium", basePlanId="monthly").execute()
print("offers list raw:", json.dumps(r))
