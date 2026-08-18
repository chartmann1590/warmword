from google.oauth2 import service_account
from googleapiclient.discovery import build
import json
C=service_account.Credentials.from_service_account_file(r'H:\psyc-app\play-creds\warmword-play-billing.json',scopes=['https://www.googleapis.com/auth/androidpublisher'])
S=build('androidpublisher','v3',credentials=C)
PKG='com.charles.warmwords'
e=S.edits().insert(packageName=PKG,body={}).execute(); eid=e['id']
S.edits().details().update(packageName=PKG,editId=eid,body={'contactEmail':'test@example.com'}).execute()
try:
    S.edits().commit(packageName=PKG,editId=eid).execute()
except Exception as ex:
    print('STATUS', ex.status_code if hasattr(ex,'status_code') else ex.resp.status)
    print('URI', ex.uri)
    print('CONTENT', ex.content.decode() if isinstance(ex.content,bytes) else ex.content)
