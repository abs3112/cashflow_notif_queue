import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:cashflow_notif_queue/cashflow_notif_queue_method_channel.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  MethodChannelCashflowNotifQueue platform = MethodChannelCashflowNotifQueue();
  const MethodChannel channel = MethodChannel('cashflow_notif_queue');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        return '42';
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
