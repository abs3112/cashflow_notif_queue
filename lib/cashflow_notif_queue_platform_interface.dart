import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'cashflow_notif_queue_method_channel.dart';

abstract class CashflowNotifQueuePlatform extends PlatformInterface {
  /// Constructs a CashflowNotifQueuePlatform.
  CashflowNotifQueuePlatform() : super(token: _token);

  static final Object _token = Object();

  static CashflowNotifQueuePlatform _instance = MethodChannelCashflowNotifQueue();

  /// The default instance of [CashflowNotifQueuePlatform] to use.
  ///
  /// Defaults to [MethodChannelCashflowNotifQueue].
  static CashflowNotifQueuePlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [CashflowNotifQueuePlatform] when
  /// they register themselves.
  static set instance(CashflowNotifQueuePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
