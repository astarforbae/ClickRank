package com.ecnu.clickrank

import android.accessibilityservice.AccessibilityService
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import org.json.JSONException
import org.json.JSONObject

class ClickTrackingService : AccessibilityService() {
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("AppClickData", Context.MODE_PRIVATE)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event != null && event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            val appName = event.packageName?.toString()
            if (appName != null && appName == "com.bbk.launcher2") {
                val packageName = getForegroundAppPackageName()
                val readableName = packageName?.let { getAppReadableName(it) }
                if (readableName == "系统桌面" || readableName == "ClickRank")
                    return

                // 获取当前存储的 JSON 数据
                val jsonData = sharedPreferences.getString(readableName, null)
                var clickCount = 0

                if (jsonData != null) {
                    try {
                        val jsonObject = JSONObject(jsonData)
                        clickCount = jsonObject.getInt("clickCount")
                    } catch (e: JSONException) {
                        Log.e("ClickTracking", "Error parsing JSON for app: $readableName", e)
                    }
                }

                // 更新点击次数
                clickCount++

                // 构造新的 JSON 数据
                val updatedData = """
                {
                    "appName": "$readableName",
                    "packageName": "$packageName",
                    "clickCount": $clickCount
                }
            """.trimIndent()

                // 写入更新后的 JSON 数据
                sharedPreferences.edit()
                    .putString(readableName, updatedData)
                    .apply()

                Log.d(
                    "ClickTracking",
                    "Foreground app: $readableName (Package: $packageName), total clicks: $clickCount"
                )
            }
        }
    }

    override fun onInterrupt() {
        // 如果服务中断，可以做一些处理
    }

    private fun getForegroundAppPackageName(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 3600, time)
        if (stats != null && stats.isNotEmpty()) {
            val recentStats = stats.maxByOrNull { it.lastTimeUsed }
            return recentStats?.packageName
        }
        return null
    }

    private fun getAppReadableName(packageName: String): String {
        return try {
            val packageManager = packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: Exception) {
            Log.e("ClickTracking", "Failed to get readable name for package: $packageName", e)
            packageName
        }
    }
}
