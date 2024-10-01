import 'dart:async';
import 'dart:developer';

import 'package:flutter/services.dart';
import 'package:flutter_overlay_window2/src/models/overlay_position.dart';
import 'package:flutter_overlay_window2/src/overlay_config.dart';

class FlutterOverlayWindow2 {
  FlutterOverlayWindow2._();

  static final StreamController _controller = StreamController();
  static const MethodChannel _channel =
      MethodChannel("x-slayer/overlay_channel2");
  static const MethodChannel _overlayChannel =
      MethodChannel("x-slayer/overlay2");
  static const BasicMessageChannel _overlayMessageChannel =
      BasicMessageChannel("x-slayer/overlay_messenger2", JSONMessageCodec());

  /// Open overLay content
  ///
  /// - Optional arguments:
  ///
  /// `height` the overlay height and default is [WindowSize2.fullCover]
  ///
  /// `width` the overlay width and default is [WindowSize2.matchParent]
  ///
  /// `alignment` the alignment postion on screen and default is [OverlayAlignment2.center]
  ///
  /// `visibilitySecret` the detail displayed in notifications on the lock screen and default is [NotificationVisibility2.visibilitySecret]
  ///
  /// `OverlayFlag` the overlay flag and default is [OverlayFlag2.defaultFlag]
  ///
  /// `overlayTitle` the notification message and default is "overlay activated"
  ///
  /// `overlayContent` the notification message
  ///
  /// `enableDrag` to enable/disable dragging the overlay over the screen and default is "false"
  ///
  /// `positionGravity` the overlay postion after drag and default is [PositionGravity2.none]
  ///
  /// `startPosition` the overlay start position and default is null
  static Future<void> showOverlay({
    int height = WindowSize2.fullCover,
    int width = WindowSize2.matchParent,
    OverlayAlignment2 alignment = OverlayAlignment2.center,
    NotificationVisibility2 visibility = NotificationVisibility2.visibilitySecret,
    OverlayFlag2 flag = OverlayFlag2.defaultFlag,
    String overlayTitle = "overlay activated",
    String? overlayContent,
    bool enableDrag = false,
    PositionGravity2 positionGravity = PositionGravity2.none,
    OverlayPosition2? startPosition,
  }) async {
    await _channel.invokeMethod(
      'showOverlay',
      {
        "height": height,
        "width": width,
        "alignment": alignment.name,
        "flag": flag.name,
        "overlayTitle": overlayTitle,
        "overlayContent": overlayContent,
        "enableDrag": enableDrag,
        "notificationVisibility": visibility.name,
        "positionGravity": positionGravity.name,
        "startPosition": startPosition?.toMap(),
      },
    );
  }

  /// Check if overlay permission is granted
  static Future<bool> isPermissionGranted() async {
    try {
      return await _channel.invokeMethod<bool>('checkPermission') ?? false;
    } on PlatformException catch (error) {
      log("$error");
      return Future.value(false);
    }
  }

  /// Request overlay permission
  /// it will open the overlay settings page and return `true` once the permission granted.
  static Future<bool?> requestPermission() async {
    try {
      return await _channel.invokeMethod<bool?>('requestPermission');
    } on PlatformException catch (error) {
      log("Error requestPermession: $error");
      rethrow;
    }
  }

  /// Closes overlay if open
  static Future<bool?> closeOverlay() async {
    final bool? _res = await _channel.invokeMethod('closeOverlay');
    return _res;
  }

  /// Broadcast data to and from overlay app
  static Future shareData(dynamic data) async {
    return await _overlayMessageChannel.send(data);
  }

  /// Streams message shared between overlay and main app
  static Stream<dynamic> get overlayListener {
    _overlayMessageChannel.setMessageHandler((message) async {
      _controller.add(message);
      return message;
    });
    return _controller.stream;
  }

  /// Update the overlay flag while the overlay in action
  static Future<bool?> updateFlag(OverlayFlag2 flag) async {
    final bool? _res = await _overlayChannel
        .invokeMethod<bool?>('updateFlag', {'flag': flag.name});
    return _res;
  }

  /// Update the overlay size in the screen
  static Future<bool?> resizeOverlay(
    int width,
    int height,
    bool enableDrag,
  ) async {
    final bool? _res = await _overlayChannel.invokeMethod<bool?>(
      'resizeOverlay',
      {
        'width': width,
        'height': height,
        'enableDrag': enableDrag,
      },
    );
    return _res;
  }

  /// Update the overlay position in the screen
  ///
  /// `position` the new position of the overlay
  ///
  /// `return` true if the position updated successfully
  static Future<bool?> moveOverlay(OverlayPosition2 position) async {
    final bool? _res = await _channel.invokeMethod<bool?>(
      'moveOverlay',
      position.toMap(),
    );
    return _res;
  }

  /// Get the current overlay position
  ///
  /// `return` the current overlay position
  static Future<OverlayPosition2> getOverlayPosition() async {
    final Map<Object?, Object?>? _res = await _channel.invokeMethod(
      'getOverlayPosition',
    );
    return OverlayPosition2.fromMap(_res);
  }

  /// Check if the current overlay is active
  static Future<bool> isActive() async {
    final bool? _res = await _channel.invokeMethod<bool?>('isOverlayActive');
    return _res ?? false;
  }

  /// Dispose overlay stream
  static void disposeOverlayListener() {
    _controller.close();
  }
}
