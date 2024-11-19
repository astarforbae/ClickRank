package com.ecnu.clickrank

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.provider.Settings
import android.util.Log
import android.app.AppOpsManager
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    private lateinit var adapter: AppClickAdapter
    private val clickDataList = mutableListOf<AppClickData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clearSharedPreferences()

        // 初始化 RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        adapter = AppClickAdapter(clickDataList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 检查权限并加载数据
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        // 在 Activity 恢复到前台时重新加载数据
        fetchDataFromPreferences()
    }

    private fun checkPermissions() {
        // 检查辅助功能权限
        if (!isAccessibilityPermissionGranted()) {
            requestAccessibilityPermission()
        } else {
            Toast.makeText(this, "辅助功能权限已开启", Toast.LENGTH_SHORT).show()
        }

        // 检查电池优化忽略权限
        if (!isIgnoringBatteryOptimizations()) {
            requestIgnoreBatteryOptimization()
        } else {
            Toast.makeText(this, "已忽略电池优化", Toast.LENGTH_SHORT).show()
        }

        // 检查使用访问权限
        if (!isUsageAccessPermissionGranted()) {
            requestUsageAccessPermission()
        } else {
            Toast.makeText(this, "使用访问权限已开启", Toast.LENGTH_SHORT).show()
        }
    }

    // 从 SharedPreferences 获取并加载点击数据
    @SuppressLint("NotifyDataSetChanged")
    private fun fetchDataFromPreferences() {
        val sharedPreferences = getSharedPreferences("AppClickData", Context.MODE_PRIVATE)
        val allEntries = sharedPreferences.all
        val dataList = mutableListOf<AppClickData>()

        for ((key, value) in allEntries) {
            if (value is String) { // 确保值是 JSON 格式字符串
                try {
                    val jsonObject = JSONObject(value)
                    val appName = jsonObject.getString("appName")
                    val packageName = jsonObject.getString("packageName")
                    val clickCount = jsonObject.getInt("clickCount")
                    val appIcon = getAppIconByName(packageName) // 根据包名获取图标

                    dataList.add(AppClickData(appName, clickCount, appIcon))
                } catch (e: JSONException) {
                    Log.e("FetchPreferences", "Error parsing JSON for key: $key", e)
                }
            }
        }

        // 更新 RecyclerView 数据源
        clickDataList.clear()
        clickDataList.addAll(dataList.sortedByDescending { it.clickCount }) // 根据点击数降序排序
        adapter.notifyDataSetChanged() // 通知适配器数据已更新
    }


    // 获取应用图标（根据包名）
    private fun getAppIconByName(packageName: String): Drawable? {
        return try {
            val packageManager = packageManager
            packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("MainActivity", "App icon not found for package: $packageName", e)
            resources.getDrawable(R.drawable.default_icon, null)  // 使用默认图标
        }
    }
    // 清空 SharedPreferences 中的数据
    private fun clearSharedPreferences() {
        val sharedPreferences = getSharedPreferences("AppClickData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()  // 清空所有数据
        editor.apply()  // 应用更改
    }

    // 检查是否授予了辅助功能权限
    private fun isAccessibilityPermissionGranted(): Boolean {
        val serviceName = "$packageName/${ClickTrackingService::class.java.name}"
        val enabledServices =
            Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabledServices != null && enabledServices.contains(serviceName)
    }

    // 检查是否忽略了电池优化
    private fun isIgnoringBatteryOptimizations(): Boolean {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    // 检查是否已启用使用访问权限
    private fun isUsageAccessPermissionGranted(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // 引导用户进入辅助功能权限设置
    private fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    // 引导用户进入电池优化设置
    private fun requestIgnoreBatteryOptimization() {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        startActivity(intent)
    }

    // 引导用户进入使用访问权限设置
    private fun requestUsageAccessPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }
}
