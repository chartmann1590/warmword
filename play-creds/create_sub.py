import os
from google.oauth2 import service_account
from googleapiclient.discovery import build

KEY = r"H:\psyc-app\play-creds\warmword-play-billing.json"
PKG = "com.charles.warmwords"
C = service_account.Credentials.from_service_account_file(KEY,
    scopes=["https://www.googleapis.com/auth/androidpublisher"])
S = build("androidpublisher","v3",credentials=C)

sub = {
  "productId": "warmword_premium",
  "listings": [{
     "languageCode":"en-US",
     "title":"WarmWord Premium",
     "description":"Unlock unlimited conversations and deeper mood insights.",
     "benefits":["Unlimited private AI chats","Unlimited journaling","Advanced mood insights","Priority on-device model"]
  }],
  "basePlans": [{
     "basePlanId":"monthly",
     "autoRenewingBasePlanType":{
        "billingPeriodDuration":"P1M",
        "prorationMode":"SUBSCRIPTION_PRORATION_MODE_CHARGE_ON_NEXT_BILLING_DATE",
        "gracePeriodDuration":"P3D",
        "accountHoldDuration":"P30D"
     },
     "regionalConfigs":[{
        "regionCode":"US",
        "newSubscriberAvailability":True,
        "price":{"currencyCode":"USD","units":"9","nanos":990000000}
     }]
  }]
}
try:
    r = S.monetization().subscriptions().create(packageName=PKG, productId="warmword_premium", regionsVersion_version="2025/03", body=sub).execute()
    print("CREATED", r.get("productId"))
except Exception as ex:
    print("ERR", repr(ex)[:1500])
