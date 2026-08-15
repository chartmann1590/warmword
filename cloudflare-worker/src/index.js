/**
 * WarmWord key proxy.
 *
 * Keeps Google Places / Maps API keys out of the Android app entirely. The app calls this
 * Worker's public URL with no secret attached; the Worker injects the real key (stored as a
 * Cloudflare secret, never committed to source control) and forwards the request to Google.
 *
 * Routes:
 *   GET /places/nearby?lat=..&lng=..&keyword=..   -> Google Places Nearby Search
 *   GET /places/details?place_id=..               -> Google Places Details
 *
 * Deploy (requires your own Cloudflare account, not something this repo can do for you):
 *   cd cloudflare-worker
 *   npm install -g wrangler   # if you don't already have it
 *   wrangler login
 *   wrangler secret put PLACES_API_KEY
 *   wrangler deploy
 */

const GOOGLE_PLACES_BASE = "https://maps.googleapis.com/maps/api/place";

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

    return jsonResponse({ error: "not found" }, 404);
  },
};
