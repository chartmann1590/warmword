/**
 * WarmWord key proxy.
 *
 * Keeps Google Places / Maps and Play Billing API secrets out of the Android app entirely.
 * The app calls this Worker's public URL with no secret attached; the Worker injects the real
 * key (stored as a Cloudflare secret, never committed to source control) and forwards the
 * request upstream.
 *
 * Routes:
 *   GET  /places/nearby?lat=..&lng=..&keyword=..   -> Google Places Nearby Search
 *   GET  /places/details?place_id=..               -> Google Places Details
 *   POST /billing/verify                           -> Verify a Play subscription purchase token
 *                                                    against the Play Developer API. Body:
 *                                                    { packageName, productId, purchaseToken }
 *
 * Deploy:
 *   cd cloudflare-worker
 *   npm install -g wrangler
 *   wrangler login
 *   wrangler secret put PLACES_API_KEY
 *   wrangler secret put ALLOWED_APP_ID
 *   wrangler secret put PLAY_SERVICE_ACCOUNT_JSON   # Google Play service-account JSON
 *   wrangler deploy
 */

const GOOGLE_PLACES_BASE = "https://maps.googleapis.com/maps/api/place";
const PLAY_SCOPE = "https://www.googleapis.com/auth/androidpublisher";
const ANDROID_PUBLISHER_BASE = "https://androidpublisher.googleapis.com/androidpublisher/v3";

// Simple app identity check - NOT real auth. For production, swap this for Firebase App
// Check (https://firebase.google.com/docs/app-check) so only genuine, unmodified installs
// of the app can call this Worker.
function isAllowedRequest(request, env) {
  const appId = request.headers.get("X-WarmWord-App-Id");
  return appId === env.ALLOWED_APP_ID;
}

function jsonResponse(body, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "content-type": "application/json" },
  });
}

async function handleNearby(url, env) {
  const lat = url.searchParams.get("lat");
  const lng = url.searchParams.get("lng");
  const keyword = url.searchParams.get("keyword") ?? "therapist";
  const radius = url.searchParams.get("radius") ?? "8000";

  if (!lat || !lng) {
    return jsonResponse({ error: "lat and lng are required" }, 400);
  }

  const upstream = new URL(`${GOOGLE_PLACES_BASE}/nearbysearch/json`);
  upstream.searchParams.set("location", `${lat},${lng}`);
  upstream.searchParams.set("radius", radius);
  upstream.searchParams.set("keyword", keyword);
  upstream.searchParams.set("key", env.PLACES_API_KEY);

  const upstreamResponse = await fetch(upstream.toString());
  const data = await upstreamResponse.json();
  return jsonResponse(data, upstreamResponse.status);
}

async function handleDetails(url, env) {
  const placeId = url.searchParams.get("place_id");
  if (!placeId) {
    return jsonResponse({ error: "place_id is required" }, 400);
  }

  const upstream = new URL(`${GOOGLE_PLACES_BASE}/details/json`);
  upstream.searchParams.set("place_id", placeId);
  upstream.searchParams.set(
    "fields",
    "name,formatted_address,formatted_phone_number,website,opening_hours"
  );
  upstream.searchParams.set("key", env.PLACES_API_KEY);

  const upstreamResponse = await fetch(upstream.toString());
  const data = await upstreamResponse.json();
  return jsonResponse(data, upstreamResponse.status);
}

// --- Play Billing verification ------------------------------------------------

function b64urlEncode(input) {
  return btoa(input).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

function b64urlEncodeBytes(bytes) {
  let binary = "";
  const chunk = 0x8000;
  for (let i = 0; i < bytes.length; i += chunk) {
    binary += String.fromCharCode.apply(null, bytes.subarray(i, i + chunk));
  }
  return b64urlEncode(binary)
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
}

function pemToDer(pem) {
  const b64 = pem
    .replace(/-----BEGIN[^-]+-----/, "")
    .replace(/-----END[^-]+-----/, "")
    .replace(/\s+/g, "");
  const binary = atob(b64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
  return bytes;
}

async function getAccessToken(serviceAccount) {
  const now = Math.floor(Date.now() / 1000);
  const header = b64urlEncode(JSON.stringify({ alg: "RS256", typ: "JWT" }));
  const claim = b64urlEncode(
    JSON.stringify({
      iss: serviceAccount.client_email,
      scope: PLAY_SCOPE,
      aud: serviceAccount.token_uri || "https://oauth2.googleapis.com/token",
      exp: now + 3600,
      iat: now,
    })
  );
  const signingInput = `${header}.${claim}`;

  const key = await crypto.subtle.importKey(
    "pkcs8",
    pemToDer(serviceAccount.private_key),
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"]
  );
  const sig = await crypto.subtle.sign(
    "RSASSA-PKCS1-v1_5",
    key,
    new TextEncoder().encode(signingInput)
  );
  const jwt = `${signingInput}.${b64urlEncodeBytes(new Uint8Array(sig))}`;

  const tokenRes = await fetch(serviceAccount.token_uri || "https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "content-type": "application/x-www-form-urlencoded" },
    body: `grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Ajwt-bearer&assertion=${jwt}`,
  });
  const tokenJson = await tokenRes.json();
  if (!tokenJson.access_token) {
    throw new Error("No access_token from Google OAuth");
  }
  return tokenJson.access_token;
}

async function verifyPurchase(env, packageName, productId, purchaseToken) {
  const serviceAccount = JSON.parse(env.PLAY_SERVICE_ACCOUNT_JSON);
  const accessToken = await getAccessToken(serviceAccount);

  const url = `${ANDROID_PUBLISHER_BASE}/applications/${packageName}/purchases/subscriptionsv2/tokens/${purchaseToken}`;
  const res = await fetch(url, {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  if (!res.ok) {
    return jsonResponse({ isValid: false, error: `Play API ${res.status}` }, 200);
  }
  const data = await res.json();

  const state = data.subscriptionState || "";
  // ACTIVE or CANCELED (canceled is still entitled until its expiry) count as valid.
  const isValid = state === "SUBSCRIPTION_STATE_ACTIVE" || state === "SUBSCRIPTION_STATE_CANCELED";

  const lineItem = data.lineItems && data.lineItems[0];
  const expiryTimeMillis = lineItem?.expiryTime ? Date.parse(lineItem.expiryTime) : 0;
  const isAutoRenewing = state === "SUBSCRIPTION_STATE_ACTIVE";
  const resolvedProductId = lineItem?.productId || productId;

  return jsonResponse({
    isValid,
    expiryTimeMillis,
    isAutoRenewing,
    productId: resolvedProductId,
  });
}

async function handleBillingVerify(request, env) {
  if (request.method !== "POST") {
    return jsonResponse({ error: "method not allowed" }, 405);
  }
  let body;
  try {
    body = await request.json();
  } catch {
    return jsonResponse({ error: "invalid json" }, 400);
  }
  const { packageName, productId, purchaseToken } = body;
  if (!packageName || !productId || !purchaseToken) {
    return jsonResponse({ error: "packageName, productId and purchaseToken are required" }, 400);
  }
  if (!env.PLAY_SERVICE_ACCOUNT_JSON) {
    return jsonResponse({ error: "server not configured" }, 500);
  }
  try {
    return await verifyPurchase(env, packageName, productId, purchaseToken);
  } catch (e) {
    return jsonResponse({ isValid: false, error: String(e && e.message || e) }, 200);
  }
}

export default {
  async fetch(request, env) {
    if (!isAllowedRequest(request, env)) {
      return jsonResponse({ error: "unauthorized" }, 401);
    }

    const url = new URL(request.url);

    if (url.pathname === "/places/nearby") {
      return handleNearby(url, env);
    }
    if (url.pathname === "/places/details") {
      return handleDetails(url, env);
    }
    if (url.pathname === "/billing/verify") {
      return handleBillingVerify(request, env);
    }

    return jsonResponse({ error: "not found" }, 404);
  },
};
