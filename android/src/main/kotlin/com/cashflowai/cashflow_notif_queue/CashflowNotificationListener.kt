package com.cashflowai.cashflow_notif_queue

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

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

            val title = extras?.getString(Notification.EXTRA_TITLE)
                ?: extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                ?: ""

            val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                ?: extras?.getString(Notification.EXTRA_TEXT)
                ?: ""

            val bigText = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
            val subText = extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""

            val combined = listOf(title, text, bigText, subText)
                .filter { it.isNotBlank() }
                .joinToString(" • ")
                .trim()

            if (combined.isBlank()) return

            // --- FILTERS (stop junk early) ---
            // 1) hard deny list (delivery/visitor/security/activity etc.)
            if (isHardJunk(combined)) return

            // 2) ignore promos/marketing
            if (isLikelyPromo(combined)) return

            // 3) ignore OTP-style notifications
            if (isLikelyOtp(combined)) return

            // 4) require transactional-ish signals OR a parsable amount with currency
            val amount = extractAmount(combined)
            val looksTransactional = isLikelyTransactional(combined)
            if (!looksTransactional && amount <= 0.0) return

            // Keep minimal fields
            val obj = JSONObject()
                .put("pkg", sbn.packageName ?: "")
                .put("postTime", sbn.postTime)
                .put("title", title)
                .put("text", combined)
                .put("amount", amount)

            NotifQueueStore.append(applicationContext, obj)
        } catch (_: Throwable) {
            // NEVER crash system callback
        }
    }

    // ---------------- Helpers ----------------

    private fun norm(s: String): String =
        s.lowercase(Locale.ROOT).replace("\n", " ").trim()

    private fun isHardJunk(s: String): Boolean {
        val x = norm(s)

        // Delivery / gate / society / visitor / parcel type noise etc.
        val junkNeedles = listOf(
            "checked out", "check out", "checked-in", "checked in",
            "main gate", "gate", "security", "visitor", "delivery",
            "parcel", "courier", "arrived", "left", "exit", "entered",
            "society", "rwa", "tower", "flat", "apartment",
            "amazon has", "swiggy", "zomato", "blinkit", "zepto",
            "ride", "driver", "cab", "ola", "uber"
        )
        return junkNeedles.any { x.contains(it) }
    }

    private fun isLikelyPromo(s: String): Boolean {
        val x = norm(s)
        val promos = listOf(
            "offer", "discount", "sale", "coupon", "cashback if",
            "limited time", "hurry", "deal", "subscribe",
            "buy now", "shop now", "click here", "tap to",
            "ad ", "advertisement", "promo", "promotion", "win "
        )
        return promos.any { x.contains(it) }
    }

    private fun isLikelyOtp(s: String): Boolean {
        val x = norm(s)
        // OTP patterns + common OTP words
        if (x.contains(" otp")) return true
        if (x.contains("one time password")) return true
        if (Regex("""\botp\b""", RegexOption.IGNORE_CASE).containsMatchIn(s)) return true
        // 4-8 digit code often used as OTP (avoid being too aggressive)
        if (Regex("""\b\d{4,8}\b""").containsMatchIn(s) && x.contains("code")) return true
        return false
    }

    private fun isLikelyTransactional(s: String): Boolean {
        val x = norm(s)

        // Strong transaction indicators
        val needles = listOf(
            "debited", "credited", "spent", "received", "refund",
            "upi", "txn", "transaction", "payment", "paid",
            "imps", "neft", "rtgs", "ach", "nach",
            "card", "pos", "atm", "withdrawn", "deposit",
            "balance", "bal.", "avl bal", "a/c", "acct", "account",
            "bank", "statement"
        )
        return needles.any { x.contains(it) }
    }

    /**
     * Extract amount for major currencies: INR, AED, USD, GBP, EUR, CAD (CA$).
     * Supports currency prefix or suffix.
     */
    private fun extractAmount(s: String): Double {
        val pattern = Regex(
            // currency BEFORE number
            """(?:rs\.?|inr|₹|aed|usd|\$|gbp|£|eur|€|cad|ca\$|c\$)\s*([+-]?(?:\d{1,3}(?:,\d{3})+|\d+)(?:\.\d{1,2})?)""" +
                    "|" +
                    // number BEFORE currency
                    """([+-]?(?:\d{1,3}(?:,\d{3})+|\d+)(?:\.\d{1,2})?)\s*(?:rs\.?|inr|₹|aed|usd|\$|gbp|£|eur|€|cad|ca\$|c\$)""",
            RegexOption.IGNORE_CASE
        )

        val m = pattern.find(s) ?: return 0.0
        val raw = (m.groups[1]?.value ?: m.groups[2]?.value ?: "")
            .replace(",", "")
            .trim()

        return raw.toDoubleOrNull() ?: 0.0
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