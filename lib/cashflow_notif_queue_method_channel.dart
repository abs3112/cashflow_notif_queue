import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'cashflow_notif_queue_platform_interface.dart';

class MethodChannelCashflowNotifQueue extends CashflowNotifQueuePlatform {
  @visibleForTesting
  final methodChannel = const MethodChannel('cashflow_notif_queue');

  @override
  Future<String?> getPlatformVersion() async {
    return methodChannel.invokeMethod<String>('getPlatformVersion');
  }

  @override
  Future<bool> openNotificationListenerSettings() async {
    final v = await methodChannel.invokeMethod<bool>('openNotificationListenerSettings');
    return v ?? false;
  }

  @override
  Future<bool> isListenerConnected() async {
    final v = await methodChannel.invokeMethod<bool>('isListenerConnected');
    return v ?? false;
  }

  @override
  Future<int> peekCount() async {
    final v = await methodChannel.invokeMethod<int>('peekCount');
    return v ?? 0;
  }

  @override
  Future<String?> drain() async {
    return methodChannel.invokeMethod<String>('drain');
  }

  @override
  Future<bool> clear() async {
    final v = await methodChannel.invokeMethod<bool>('clear');
    return v ?? false;
  }
}