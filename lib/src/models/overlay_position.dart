import 'package:flutter/foundation.dart';

@immutable
class OverlayPosition2 {
  final double x;
  final double y;

  const OverlayPosition2(this.x, this.y);

  factory OverlayPosition2.fromMap(Map<Object?, Object?>? map) =>
      OverlayPosition2(map?['x'] as double? ?? 0, map?['y'] as double? ?? 0);

  Map<String, dynamic> toMap() =>
      <String, dynamic>{'x': x.toInt(), 'y': y.toInt()};

  @override
  String toString() {
    return 'OverlayPosition{x=$x, y=$y}';
  }
}
