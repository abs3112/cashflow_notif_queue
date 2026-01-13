import 'package:flutter_test/flutter_test.dart';
import 'package:cashflow_notif_queue/cashflow_notif_queue.dart';
import 'package:cashflow_notif_queue/cashflow_notif_queue_platform_interface.dart';
import 'package:cashflow_notif_queue/cashflow_notif_queue_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockCashflowNotifQueuePlatform
    with MockPlatformInterfaceMixin
    implements CashflowNotifQueuePlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final CashflowNotifQueuePlatform initialPlatform = CashflowNotifQueuePlatform.instance;

  test('$MethodChannelCashflowNotifQueue is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelCashflowNotifQueue>());
  });

  test('getPlatformVersion', () async {
    CashflowNotifQueue cashflowNotifQueuePlugin = CashflowNotifQueue();
    MockCashflowNotifQueuePlatform fakePlatform = MockCashflowNotifQueuePlatform();
    CashflowNotifQueuePlatform.instance = fakePlatform;

    expect(await cashflowNotifQueuePlugin.getPlatformVersion(), '42');
  });
}
