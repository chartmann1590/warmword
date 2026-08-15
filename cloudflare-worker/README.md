# WarmWord Key Proxy (Cloudflare Worker)

Keeps any third-party API keys (Google Places, Maps, etc.) out of the Android app and off
GitHub entirely. The app never holds a key — it calls this Worker's public URL, and the Worker
attaches the real key server-side (stored as an encrypted Cloudflare secret) before forwarding
to the upstream API.

WarmWord's "Find Providers" list currently ships as a static, in-app list and does not call
this Worker yet. This is here so that if/when live location-based provider search is added, the
key never has to live in the app or in source control — just point that feature at this
Worker's URL instead of calling Google directly.

## One-time setup (requires your own Cloudflare account — this repo cannot do this for you)

```bash
cd cloudflare-worker
npm install -g wrangler   # Cloudflare's CLI, if you don't already have it
wrangler login            # opens a browser to authenticate with YOUR Cloudflare account
wrangler secret put PLACES_API_KEY   # paste your real Google Places key when prompted
wrangler deploy
```

`wrangler deploy` prints the Worker's public URL (e.g.
`https://warmword-key-proxy.<your-subdomain>.workers.dev`). That URL is safe to hardcode in the
app — it holds no secret itself.

## Local development

```bash
wrangler dev
```

## Security notes

- `PLACES_API_KEY` is a Cloudflare secret, set via `wrangler secret put`, never written to
  `wrangler.toml` or committed to git.
- Restrict the real Google API key itself (in Google Cloud Console) to only the Places API and,
  if possible, to this Worker's outbound IP range / referrer.
- `isAllowedRequest` in `src/index.js` is a placeholder identity check, not real
  authentication. Before shipping a feature that depends on this Worker, replace it with
  [Firebase App Check](https://firebase.google.com/docs/app-check) so only genuine installs of
  the app can call it, not just anyone who finds the URL.
