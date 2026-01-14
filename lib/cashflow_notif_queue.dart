import 'cashflow_notif_queue_platform_interface.dart';

class CashflowNotifQueue {
  Future<String?> getPlatformVersion() {
    return CashflowNotifQueuePlatform.instance.getPlatformVersion();
  }

  Future<bool> openNotificationListenerSettings() {
    return CashflowNotifQueuePlatform.instance.openNotificationListenerSettings();
  }

  Future<bool> isListenerConnected() {
    return CashflowNotifQueuePlatform.instance.isListenerConnected();
  }

  Future<int> peekCount() {
    return CashflowNotifQueuePlatform.instance.peekCount();
  }

  /// Returns JSON string (e.g. '[{...},{...}]') and clears the native queue.
  Future<String?> drain() {
    return CashflowNotifQueuePlatform.instance.drain();
  }

  Future<bool> clear() {
    return CashflowNotifQueuePlatform.instance.clear();
  }
}