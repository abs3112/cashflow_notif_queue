import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'cashflow_notif_queue_platform_interface.dart';

class MethodChannelCashflowNotifQueue extends CashflowNotifQueuePlatform {
  @visibleForTesting
  final MethodChannel methodChannel = const MethodChannel('cashflow_notif_queue');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<bool> openNotificationListenerSettings() async {
    final res = await methodChannel.invokeMethod<bool>('openNotificationListenerSettings');
    return res ?? false;
  }

  @override
  Future<bool> isListenerConnected() async {
    final res = await methodChannel.invokeMethod<bool>('isListenerConnected');
    return res ?? false;
  }

  @override
  Future<int> peekCount() async {
    final res = await methodChannel.invokeMethod<dynamic>('peekCount');
    if (res is int) return res;
    if (res is num) return res.toInt();
    return 0;
  }

  @override
  Future<String> drain() async {
    final res = await methodChannel.invokeMethod<String>('drain');
    return res ?? '[]';
  }

  @override
  Future<bool> clear() async {
    final res = await methodChannel.invokeMethod<bool>('clear');
    return res ?? false;
  }
}