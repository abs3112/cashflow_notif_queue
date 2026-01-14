package com.cashflowai.cashflow_notif_queue

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import org.json.JSONArray

class CashflowNotifQueuePlugin : FlutterPlugin, MethodChannel.MethodCallHandler {

  private lateinit var channel: MethodChannel
  private var appContext: Context? = null

  companion object {
    // Must match whatever you use on the Dart side
    private const val CHANNEL_NAME = "cashflow_notif_queue"

    // Must match what you used in CashflowNotificationListener.kt
    private const val PREFS = "cashflow_notif_queue_prefs"
    private const val KEY_QUEUE = "queue_json"
    private const val KEY_CONNECTED = "listener_connected"
  }

  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    appContext = binding.applicationContext
    channel = MethodChannel(binding.binaryMessenger, CHANNEL_NAME)
    channel.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    appContext = null
  }

  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    val ctx = appContext
    if (ctx == null) {
      result.error("NO_CONTEXT", "Plugin not attached to engine yet.", null)
      return
    }

    when (call.method) {
      // Opens the system screen where user enables notification access
      // Using string action avoids API-level constant issues.
      // (ACTION_NOTIFICATION_LISTENER_SETTINGS is the correct settings page)
      //
      "openNotificationListenerSettings" -> {
        try {
          val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          ctx.startActivity(intent)
          result.success(true)
        } catch (t: Throwable) {
          result.error("OPEN_SETTINGS_FAILED", t.message, null)
        }
      }

      // Returns true/false based on what your listener writes to prefs
      "isListenerConnected" -> {
        val connected = prefs(ctx).getBoolean(KEY_CONNECTED, false)
        result.success(connected)
      }

      // Returns count of queued notifications
      "peekCount" -> {
        val arr = getQueue(ctx)
        result.success(arr.length())
      }

      // Returns the queue JSON string and clears it
      "drain" -> {
        val p = prefs(ctx)
        val arr = getQueue(ctx)
        p.edit().putString(KEY_QUEUE, "[]").apply()
        result.success(arr.toString()) // return JSON string to Dart
      }

      // Clears queue
      "clear" -> {
        prefs(ctx).edit().putString(KEY_QUEUE, "[]").apply()
        result.success(true)
      }

      else -> result.notImplemented()
    }
  }

  private fun prefs(ctx: Context): SharedPreferences =
    ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

  private fun getQueue(ctx: Context): JSONArray {
    val raw = prefs(ctx).getString(KEY_QUEUE, "[]") ?: "[]"
    return try {
      JSONArray(raw)
    } catch (_: Throwable) {
      JSONArray()
    }
  }
}