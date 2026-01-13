
import 'cashflow_notif_queue_platform_interface.dart';

class CashflowNotifQueue {
  Future<String?> getPlatformVersion() {
    return CashflowNotifQueuePlatform.instance.getPlatformVersion();
  }
}
