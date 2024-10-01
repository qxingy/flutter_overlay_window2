import 'package:flutter/foundation.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_overlay_window2/flutter_overlay_window2.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('FlutterOverlayWindow', () {
    test('closeOverlay should close the overlay', () async {
      await FlutterOverlayWindow2.closeOverlay();
      expect(await FlutterOverlayWindow2.isActive(), isFalse);
    });

    if (defaultTargetPlatform == TargetPlatform.android) {
      test('isPermissionGranted should return a boolean', () async {
        final result = await FlutterOverlayWindow2.isPermissionGranted();
        expect(result, isA<bool>());
      });
    }
    test('requestPermission should return a boolean', () async {
      final result = await FlutterOverlayWindow2.requestPermission();
      expect(result, isA<bool>());
    });
  });
}
