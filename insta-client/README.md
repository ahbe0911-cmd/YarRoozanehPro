# Insta Client

Android Instagram web client with an app-local sing-box VMess transport. The
proxy applies only to this app's WebView and does not create a device VPN.

The VMess profile is entered on first launch and remains in app-private storage;
credentials are deliberately excluded from the public source and CI logs.
