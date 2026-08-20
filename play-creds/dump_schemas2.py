import json
from googleapiclient.discovery import build
from google.oauth2 import service_account

KEY = r"H:\psyc-app\play-creds\warmword-play-billing.json"
C = service_account.Credentials.from_service_account_file(KEY, scopes=["https://www.googleapis.com/auth/androidpublisher"])
S = build("androidpublisher", "v3", credentials=C)
sc = S._rootDesc["schemas"]
for name in ["RegionalSubscriptionOfferPhaseConfig", "OfferPhase", "OtherRegionsSubscriptionOfferPhaseConfig",
             "Pricing", "Money", "InstallmentPlan", "BasePlan"]:
    if name in sc:
        d = sc[name]
        print("=" * 8, name, "=" * 8)
        print((d.get("description") or "")[:120])
        for k, v in d.get("properties", {}).items():
            ref = v.get("$ref", v.get("type", ""))
            print("   ", k, "->", ref, (v.get("description") or "")[:70])
