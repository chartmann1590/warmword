from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload
from google_auth_httplib2 import AuthorizedHttp
import httplib2

KEY = "H:/psyc-app/play-creds/warmword-play-billing.json"
PKG = "com.charles.warmwords"
AAB = "H:/psyc-app/app/build/outputs/bundle/release/app-release.aab"

C = service_account.Credentials.from_service_account_file(
    KEY, scopes=["https://www.googleapis.com/auth/androidpublisher"])
http = httplib2.Http(timeout=600)
auth_http = AuthorizedHttp(C, http=http)
S = build("androidpublisher", "v3", http=auth_http)

eid = S.edits().insert(packageName=PKG, body={}).execute()["id"]
print("edit", eid)

up = S.edits().bundles().upload(
    packageName=PKG, editId=eid,
    media_body=MediaFileUpload(AAB, mimetype="application/octet-stream")
).execute()
vc = up["versionCode"]
print("uploaded versionCode", vc)

# Production track as a DRAFT release (not completed, not rolled out).
S.edits().tracks().update(
    packageName=PKG, editId=eid, track="production",
    body={"releases": [{"versionCodes": [vc], "status": "draft",
                        "name": "1.1.0",
                        "releaseNotes": [{
                            "language": "en-US",
                            "text": "New: translate the whole app (every menu, button and screen) into a language of your choice, on-device and private, using ML Kit. Also updates the app icon to match our branding, adds in-app language selection, and re-translates automatically when the translation model is ready."
                        }]}]}
).execute()
print("track production updated (draft)")

res = S.edits().commit(packageName=PKG, editId=eid).execute()
print("COMMITTED", res.get("id"))
