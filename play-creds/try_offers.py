import json
from google.oauth2 import service_account
from googleapiclient.discovery import build

KEY = r"H:\psyc-app\play-creds\warmword-play-billing.json"
PKG = "com.charles.warmwords"
C = service_account.Credentials.from_service_account_file(KEY, scopes=["https://www.googleapis.com/auth/androidpublisher"])
S = build("androidpublisher", "v3", credentials=C)

def try_create(offer_id, phases, regional, tag):
    offer = {"basePlanId": "monthly", "phases": phases, "regionalConfigs": regional}
    try:
        S.monetization().subscriptions().basePlans().offers().create(
            packageName=PKG, productId="warmword_premium", basePlanId="monthly",
            offerId=offer_id, regionsVersion_version="2025/03", body=offer).execute()
        print("OK  ", tag, offer_id)
        return True
    except Exception as e:
        print("ERR ", tag, offer_id, "::", str(e).split('"Details":')[0][:120])
        return False

base_price = {"currencyCode": "USD", "units": "9", "nanos": 990000000}

# Variant A: publisher-console-style base offer, P1M at base price, no top-level regional
try_create("monthly-a", [
    {"duration": "P1M", "recurrenceCount": 1,
     "regionalConfigs": [{"regionCode": "US", "price": base_price}]}
], [{"regionCode": "US", "newSubscriberAvailability": True}], "console-style")

# Variant B: D-prefixed offerId (Play auto format), same price
# cleanup variant A id conflicts handled by unique names
