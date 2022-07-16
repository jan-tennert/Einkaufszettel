package io.github.jan.einkaufszettel.android

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.MutableState
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import io.github.jan.einkaufszettel.common.downloadFile
import io.github.jan.einkaufszettel.common.repositories.product.ProductRepository
import io.ktor.client.HttpClient
import java.io.File


class AutoUpdater(private val httpClient: HttpClient) {

    suspend fun enqueueDownload(applicationContext: Context, progress: MutableState<Float>) {
        val PATH: String = applicationContext.getExternalFilesDir(null)!!.absolutePath
        val file = File(PATH)
        val outputFile = File(file, "Einkaufszettel.apk")
        httpClient.downloadFile(outputFile, ProductRepository.URL + "/apk") {
            progress.value = it
        }
        showInstallOption(applicationContext)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun showInstallOption(
        applicationContext: Context
    ) {
        val PATH: String = applicationContext.getExternalFilesDir(null)!!.absolutePath
        val file = File("$PATH/Einkaufszettel.apk")
        val intent = Intent(Intent.ACTION_VIEW)
        if (Build.VERSION.SDK_INT >= 24) {
            val downloadedApk = FileProvider.getUriForFile(
                applicationContext,
                applicationContext.applicationContext.packageName.toString() + ".provider",
                file
            )
            intent.setDataAndType(downloadedApk, "application/vnd.android.package-archive")
            val resInfoList: List<ResolveInfo> = applicationContext.packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                applicationContext.grantUriPermission(
                    applicationContext.applicationContext.packageName.toString() + ".provider",
                    downloadedApk,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(applicationContext, intent, null)
        } else {
            intent.action = Intent.ACTION_VIEW
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(applicationContext, intent, null)
    }

}