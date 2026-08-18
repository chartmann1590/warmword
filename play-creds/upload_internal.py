from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload
from google_auth_httplib2 import AuthorizedHttp
import httplib2

KEY = "H:/psyc-app/play-creds/warmword-play-billing.json"
PKG = "com.charles.warmwords"
APK = "H:/psyc-app/app/build/outputs/bundle/release/app-release.aab"

C = service_account.Credentials.from_service_account_file(
    KEY, scopes=["https://www.googleapis.com/auth/androidpublisher"])
http = httplib2.Http(timeout=600)
auth_http = AuthorizedHttp(C, http=http)
S = build("androidpublisher", "v3", http=auth_http)

eid = S.edits().insert(packageName=PKG, body={}).execute()["id"]
print("edit", eid)

up = S.edits().bundles().upload(
    packageName=PKG, editId=eid,
    media_body=MediaFileUpload(APK, mimetype="application/octet-stream")
).execute()
vc = up["versionCode"]
print("uploaded versionCode", vc)

S.edits().tracks().update(
    packageName=PKG, editId=eid, track="internal",
    body={"releases": [{"versionCodes": [vc], "status": "completed"}]}
).execute()
print("track internal updated")

res = S.edits().commit(packageName=PKG, editId=eid).execute()
print("COMMITTED", res.get("id"))
