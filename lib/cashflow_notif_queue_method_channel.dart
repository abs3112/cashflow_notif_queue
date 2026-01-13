import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'cashflow_notif_queue_platform_interface.dart';

/// An implementation of [CashflowNotifQueuePlatform] that uses method channels.
class MethodChannelCashflowNotifQueue extends CashflowNotifQueuePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('cashflow_notif_queue');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
