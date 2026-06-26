/**
 * Copyright (c) 2024 Uber Technologies, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.uber.sdk2.core.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection

/** Helper class for Custom Tabs. */
object CustomTabsHelper {
  private var connection: CustomTabsServiceConnection? = null

  /**
   * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it on a WebView.
   *
   * @param context The host context.
   * @param customTabsIntent a CustomTabsIntent to be used if Custom Tabs is available.
   * @param uri the Uri to be opened.
   * @param fallback a CustomTabFallback to be used if Custom Tabs is not available.
   */
  fun openCustomTab(
    context: Context,
    customTabsIntent: CustomTabsIntent,
    uri: Uri,
    fallback: CustomTabFallback?,
  ) {
    val packageName = getPackageNameToUse(context)
    if (packageName != null) {
      val connection =
        object : CustomTabsServiceConnection() {
          override fun onCustomTabsServiceConnected(
            componentName: ComponentName,
            client: CustomTabsClient,
          ) {
            client.warmup(0L) // This prevents backgrounding after redirection
            customTabsIntent.intent.setPackage(packageName)
            customTabsIntent.intent.setData(uri)
            customTabsIntent.launchUrl(context, uri)
          }

          override fun onServiceDisconnected(name: ComponentName?) {}
        }
      CustomTabsClient.bindCustomTabsService(context, packageName, connection)
      this.connection = connection
    } else
      fallback?.openUri(context, uri)
        ?: Log.e(
          UBER_AUTH_LOG_TAG,
          "Use of openCustomTab without Customtab support or a fallback set",
        )
  }

  /** Called to clean up the CustomTab when the parentActivity is destroyed. */
  fun onDestroy(parentActivity: Activity) {
    connection?.let { parentActivity.unbindService(it) }
    connection = null
  }

  /**
   * Goes through all apps that handle VIEW intents and have a warmup service. Picks the one chosen
   * by the user if there is one, otherwise makes a best effort to return a valid package name.
   *
   * This is **not** threadsafe.
   *
   * @param context [Context] to use for accessing [PackageManager].
   * @return The package name recommended to use for connecting to custom tabs related components.
   */
  private fun getPackageNameToUse(context: Context): String? {
    if (packageNameToUse != null) return packageNameToUse
    val pm: PackageManager = context.packageManager
    // Get default VIEW intent handler.
    val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))
    val defaultViewHandlerInfo: ResolveInfo? = pm.resolveActivity(activityIntent, 0)
    var defaultViewHandlerPackageName: String? = null
    if (defaultViewHandlerInfo != null) {
      defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName
    }

    // Get all apps that can handle VIEW intents.
    val resolvedActivityList: List<ResolveInfo> = pm.queryIntentActivities(activityIntent, 0)
    val packagesSupportingCustomTabs: MutableList<String?> = ArrayList()
    for (info in resolvedActivityList) {
      val serviceIntent = Intent()
      serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION)
      serviceIntent.setPackage(info.activityInfo.packageName)
      if (pm.resolveService(serviceIntent, 0) != null) {
        packagesSupportingCustomTabs.add(info.activityInfo.packageName)
      }
    }

    // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
    // and service calls.
    packageNameToUse =
      when {
        packagesSupportingCustomTabs.isEmpty() -> null
        packagesSupportingCustomTabs.size == 1 -> packagesSupportingCustomTabs[0]
        !TextUtils.isEmpty(defaultViewHandlerPackageName) &&
          !hasSpecializedHandlerIntents(context, activityIntent) &&
          packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName) ->
          defaultViewHandlerPackageName
        packagesSupportingCustomTabs.contains(STABLE_PACKAGE) -> STABLE_PACKAGE
        packagesSupportingCustomTabs.contains(BETA_PACKAGE) -> BETA_PACKAGE
        else -> packagesSupportingCustomTabs[0]
      }
    return packageNameToUse
  }

  /**
   * Used to check whether there is a specialized handler for a given intent.
   *
   * @param intent The intent to check with.
   * @return Whether there is a specialized handler for the given intent.
   */
  private fun hasSpecializedHandlerIntents(context: Context, intent: Intent): Boolean {
    try {
      val pm: PackageManager = context.packageManager
      val handlers: List<ResolveInfo> =
        pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER)
      if (handlers.isEmpty()) {
        return false
      }
      handlers.forEach { resolveInfo ->
        resolveInfo.filter?.let { filter ->
          if (
            filter.countDataAuthorities() != 0 &&
              filter.countDataPaths() != 0 &&
              resolveInfo.activityInfo != null
          ) {
            return true // A suitable handler is found, return true immediately
          }
        }
      }
    } catch (e: RuntimeException) {
      Log.e(TAG, "Runtime exception while getting specialized handlers")
    }
    return false
  }

  /** Fallback that uses browser */
  class BrowserFallback : CustomTabFallback {
    override fun openUri(context: Context, uri: Uri?) {
      val intent = Intent(Intent.ACTION_VIEW, uri)
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(intent)
    }
  }

  /** To be used as a fallback to open the Uri when Custom Tabs is not available. */
  interface CustomTabFallback {
    /**
     * @param context The Context that wants to open the Uri.
     * @param uri The uri to be opened by the fallback.
     */
    fun openUri(context: Context, uri: Uri?)
  }

  private const val TAG = "CustomTabsHelper"
  private const val STABLE_PACKAGE = "com.android.chrome"
  private const val BETA_PACKAGE = "com.chrome.beta"
  private const val ACTION_CUSTOM_TABS_CONNECTION =
    "android.support.customtabs.action.CustomTabsService"
  private var packageNameToUse: String? = null

  private const val UBER_AUTH_LOG_TAG = "UberAuth"
}
