# WarmWord Privacy Policy

**Last updated:** 2026-08-16

WarmWord is an on-device AI mental-health companion. This policy explains what
data WarmWord handles, and how it is (and isn't) shared.

## 1. Data stored on your device

WarmWord runs its AI companion **entirely on your device**. All conversations,
journal entries, mood logs, and profile information are processed by an
on-device AI model and stored locally in an encrypted local database on your
phone.

Your private conversations and journal entries are **never uploaded to
WarmWord's own servers** and are **never used to personalize advertising**.

## 2. Advertising

WarmWord is supported by **Google AdMob**, a third-party advertising network.

To display ads, AdMob and its partners may collect and process information
such as:

- your advertising ID,
- IP address,
- device type and technical information,
- coarse (city-level) location, and
- general in-app activity (which screens you open).

AdMob uses this information to serve advertisements. **By default, WarmWord
requests non-personalized ads**, meaning ad selection is not based on your
personal information or on anything you have written in the app.

You can choose to allow personalized ads in **Settings → Ads**. You can also
reset or opt out of ad personalization at any time in your device's Android
settings (**Ads → Reset advertising ID / Opt out of ads personalization**).

For more details, see
[Google's advertising privacy policy](https://policies.google.com/technologies/ads)
and [Google's privacy policy](https://policies.google.com/privacy).

## 3. Voice features

If you enable **Voice Replies**, spoken responses are synthesized on-device or
by your phone's text-to-speech engine and are never recorded or sent anywhere.

If you use the microphone button to speak a message, your phone's
speech-recognition service converts it to text. On many devices this is
handled on-device, but some devices or languages route it through your phone
manufacturer's or Google's speech-recognition servers rather than WarmWord's
own servers (WarmWord does not operate any servers of its own). Review the
transcribed text before sending; it is never sent anywhere until you tap Send.

## 4. Crash reports, performance & anonymous usage analytics

WarmWord uses **Firebase Crashlytics**, **Firebase Analytics**, and **Firebase
Performance Monitoring** to catch bugs, understand which features are used, and
measure app performance.

These services only receive anonymous event names (such as "message sent" or
"persona changed"), crash stack traces, and anonymous timing data (how long a
model download or an AI reply took) — never your chat content, journal text, or
anything you have typed.

## 5. On-device translation

If you choose a translation language in **Settings** (or during onboarding),
WarmWord's replies are converted with **Google's on-device ML Kit translator**,
which runs on your phone.

A small translation model (from a few MB up to roughly 40 MB, depending on the
language) is downloaded once over your network connection. The text is processed
entirely on the device — nothing you type or read is ever sent to a server for
translation, and translations are stored only in a temporary local cache on
your phone.

## 6. Subscriptions

If you upgrade to **WarmWord Premium**, the purchase is handled entirely by
**Google Play** and billed to your Google Play account. Google Play is the
merchant of record and processes your payment information; WarmWord does **not**
receive or store your card details.

Your subscription status is verified through a **Cloudflare Worker** controlled
by WarmWord's developer, which checks the purchase with Google's servers using
the Google Play Developer API. You can manage or cancel your subscription at any
time in Google Play.

## 7. Network access

Besides the items above, the only network access WarmWord uses is:

- the one-time download of the AI model file,
- loading advertisements,
- verifying WarmWord Premium subscription purchases with Google Play,
- opening links or the dialer for the resources listed in **Find Help**.

If a future update adds live nearby-provider search, that request is routed
through a Cloudflare Worker proxy controlled by WarmWord's developer, so no API
key for that service is ever stored inside the app.

## 8. Your rights & controls

You can **export** or **permanently delete** all of your data at any time from
**Settings**.

## 9. Contact

For privacy questions, contact the WarmWord developer via the app's support
channel.
