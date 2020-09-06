package com.example.arplacer

import android.app.Activity
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ArCoreApk.InstallStatus
import com.google.ar.core.Config
import com.google.ar.core.Session
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper


class MyUtils {
    fun createArSession(activity: Activity, installRequested: Boolean): Session? {
        var session: Session? = null
        if (ARLocationPermissionHelper.hasPermission(activity)) {
            when (ArCoreApk.getInstance().requestInstall(activity, !installRequested)) {
                InstallStatus.INSTALL_REQUESTED -> return null
                InstallStatus.INSTALLED -> {
                }
            }
            session = Session(activity)
            val config = Config(session)
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            session.configure(config)
        }
        return session
    }

}