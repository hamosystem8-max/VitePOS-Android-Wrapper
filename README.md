# VitePOS Android Hardware Wrapper v0.2.1

Final simplified hardware path for VitePOS:

- USB/HID barcode scanner selected in Hardware Setup
- Epson TM-m30II over LAN/IP raw ESC/POS (default 192.168.1.175:9100)
- VitePOS runs inside the Android WebView
- Companion WordPress plugin receives scanner events and routes receipt printing to Android
- No browser/A4 print dialog for intercepted VitePOS receipts

## First setup
1. Install the companion **VitePOS POS Extensions** WordPress plugin.
2. Install this APK on the Android tablet.
3. Open the gear icon. Enter the VitePOS URL.
4. Connect the USB barcode scanner through USB-C/OTG or a powered USB hub.
5. Select the scanner from Hardware Setup and test a barcode.
6. Set Epson IP (default 192.168.1.175), port 9100, then press TEST PRINT.
7. Press SAVE AND OPEN POS.

Printer and tablet must be reachable on the same LAN for IP printing.
