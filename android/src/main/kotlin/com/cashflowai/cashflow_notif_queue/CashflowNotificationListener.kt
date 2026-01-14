package com.cashflowai.cashflow_notif_queue

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import org.json.JSONArray
import org.json.JSONObject

class CashflowNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        NotifQueueStore.setConnected(applicationContext, true)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        NotifQueueStore.setConnected(applicationContext, false)

        // requestRebind exists only on API 24+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationListenerService.requestRebind(
                ComponentName(this, CashflowNotificationListener::class.java)
            )
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val extras = sbn.notification.extras
            val title = extras?.getString(Notification.EXTRA_TITLE) ?: ""
            val text = (extras?.getCharSequence(Notification.EXTRA_TEXT) ?: "").toString()

            val obj = JSONObject()
                .put("pkg", sbn.packageName ?: "")
                .put("postTime", sbn.postTime)
                .put("title", title)
                .put("text", text)

            NotifQueueStore.append(applicationContext, obj)
        } catch (_: Throwable) {
            // NEVER crash the system callback
        }
    }
}

object NotifQueueStore {
    private const val PREFS = "cashflow_notif_queue_prefs"
    private const val KEY_QUEUE = "queue_json"
    private const val KEY_CONNECTED = "listener_connected"
    private const val MAX_QUEUE = 200

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun setConnected(ctx: Context, connected: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_CONNECTED, connected).apply()
    }

    fun isConnected(ctx: Context): Boolean =
        prefs(ctx).getBoolean(KEY_CONNECTED, false)

    fun append(ctx: Context, obj: JSONObject) {
        val p = prefs(ctx)
        val existing = p.getString(KEY_QUEUE, "[]") ?: "[]"
        val arr = JSONArray(existing)

        arr.put(obj)

        // cap size: keep last MAX_QUEUE
        if (arr.length() > MAX_QUEUE) {
            val newArr = JSONArray()
            val start = arr.length() - MAX_QUEUE
            for (i in start until arr.length()) newArr.put(arr.get(i))
            p.edit().putString(KEY_QUEUE, newArr.toString()).apply()
            return
        }

        p.edit().putString(KEY_QUEUE, arr.toString()).apply()
    }

    fun drain(ctx: Context): JSONArray {
        val p = prefs(ctx)
        val arr = JSONArray(p.getString(KEY_QUEUE, "[]") ?: "[]")
        p.edit().putString(KEY_QUEUE, "[]").apply()
        return arr
    }

    fun peekCount(ctx: Context): Int {
        val p = prefs(ctx)
        val arr = JSONArray(p.getString(KEY_QUEUE, "[]") ?: "[]")
        return arr.length()
    }
}