package com.potato.jarvis.automation

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.potato.jarvis.core.ApiException
import com.potato.jarvis.core.JarvisApi
import com.potato.jarvis.core.SecureTokenStore
import com.potato.jarvis.db.JarvisDb

class PotatoWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = try {
        val db = JarvisDb(applicationContext)
        val token = SecureTokenStore(applicationContext).read()
        val url = db.getSetting("backend_url", JarvisApi.DEFAULT_BASE_URL)
        val api = JarvisApi(url, token)
        api.diagnostics()
        if (PotatoNotificationManager(applicationContext).canPost()) {
            api.notifications(unreadOnly = true, limit = 50).forEach { item ->
                if (item.deliveredAt == null) {
                    PotatoNotificationManager(applicationContext).show(item)
                    runCatching { api.markNotificationDelivered(item.id) }
                }
            }
        }
        Result.success()
    } catch (error: ApiException) {
        when (error.statusCode) {
            408, 429 -> Result.retry()
            in 400..499 -> Result.failure()
            else -> Result.retry()
        }
    } catch (_: java.net.SocketTimeoutException) {
        Result.retry()
    } catch (_: java.io.IOException) {
        Result.retry()
    } catch (_: IllegalArgumentException) {
        Result.failure()
    } catch (_: IllegalStateException) {
        Result.failure()
    }
}
