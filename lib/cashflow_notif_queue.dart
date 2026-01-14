import 'cashflow_notif_queue_platform_interface.dart';

class CashflowNotifQueue {
  /// Opens the system screen where the user enables Notification Access.
  Future<bool> openNotificationListenerSettings() {
    return CashflowNotifQueuePlatform.instance.openNotificationListenerSettings();
  }

  /// True if Android reports the listener connected (based on your prefs flag).
  Future<bool> isListenerConnected() {
    return CashflowNotifQueuePlatform.instance.isListenerConnected();
  }

  /// How many notifications are currently queued in SharedPreferences.
  Future<int> peekCount() {
    return CashflowNotifQueuePlatform.instance.peekCount();
  }

  /// Returns the queued notifications as a JSON string AND clears the queue.
  Future<String> drain() {
    return CashflowNotifQueuePlatform.instance.drain();
  }

  /// Clears queue without returning it.
  Future<bool> clear() {
    return CashflowNotifQueuePlatform.instance.clear();
  }

  /// Keep your existing template method if you want.
  Future<String?> getPlatformVersion() {
    return CashflowNotifQueuePlatform.instance.getPlatformVersion();
  }
}