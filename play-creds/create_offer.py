import json
from google.oauth2 import service_account
from googleapiclient.discovery import build

KEY = r"H:\psyc-app\play-creds\warmword-play-billing.json"
PKG = "com.charles.warmwords"
C = service_account.Credentials.from_service_account_file(KEY, scopes=["https://www.googleapis.com/auth/androidpublisher"])
S = build("androidpublisher", "v3", credentials=C)

offer = {
    "offerId": "monthly-base",
    "basePlanId": "monthly",
    "phases": [{
        "duration": "P1M",
        "recurrenceCount": 1,
        "regionalConfigs": [{
            "regionCode": "US",
            "price": {"currencyCode": "USD", "units": "9", "nanos": 990000000}
        }]
    }],
    "regionalConfigs": [{
        "regionCode": "US",
        "newSubscriberAvailability": True
    }]
}

try:
    r = S.monetization().subscriptions().basePlans().offers().create(
        packageName=PKG, productId="warmword_premium", basePlanId="monthly",
        offerId="monthly-base",
        regionsVersion_version="2025/03", body=offer).execute()
    print("CREATED", json.dumps(r, indent=1))
except Exception as e:
    print("ERR", type(e).__name__, str(e)[:2000])

