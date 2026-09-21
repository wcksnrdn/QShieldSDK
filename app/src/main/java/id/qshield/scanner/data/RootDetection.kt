package id.qshield.scanner.data

import android.os.Build
import java.io.File

object RootDetection {
    fun isRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su"
        )
        for (path in paths) {
            if (File(path).exists()) {
                return true
            }
        }
        return false
    }
}
