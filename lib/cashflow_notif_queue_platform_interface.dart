import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'cashflow_notif_queue_method_channel.dart';

abstract class CashflowNotifQueuePlatform extends PlatformInterface {
  CashflowNotifQueuePlatform() : super(token: _token);

  static final Object _token = Object();

  static CashflowNotifQueuePlatform _instance = MethodChannelCashflowNotifQueue();

  static CashflowNotifQueuePlatform get instance => _instance;

  static set instance(CashflowNotifQueuePlatform instance) {
    PlatformInterface.verify(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('getPlatformVersion() has not been implemented.');
  }

  Future<bool> openNotificationListenerSettings() {
    throw UnimplementedError('openNotificationListenerSettings() not implemented.');
  }

  Future<bool> isListenerConnected() {
    throw UnimplementedError('isListenerConnected() not implemented.');
  }

  Future<int> peekCount() {
    throw UnimplementedError('peekCount() not implemented.');
  }

  Future<String> drain() {
    throw UnimplementedError('drain() not implemented.');
  }

  Future<bool> clear() {
    throw UnimplementedError('clear() not implemented.');
  }
}