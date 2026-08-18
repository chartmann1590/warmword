import urllib.request, json, re
url='https://androidpublisher.googleapis.com/discovery/v1/apis/androidpublisher/v3/rest'
d=json.load(urllib.request.urlopen(url))
raw=json.dumps(d)
keys=set(re.findall(r'"([a-zA-Z]*[Pp]rivacy[a-zA-Z]*)"', raw))
print('privacy keys:', keys)
for name,sch in d['schemas'].items():
    if 'privacy' in json.dumps(sch).lower():
        print('SCHEMA', name)
def walk(o,p=''):
    if isinstance(o,dict):
        for k,v in o.items():
            if 'privacy' in k.lower() or 'privacy' in str(v).lower()[:300]:
                print('PATH', p+'.'+k)
            walk(v,p+'.'+k)
    elif isinstance(o,list):
        for v in o: walk(v,p)
walk(d)
