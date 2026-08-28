package com.asteam.toolbox.system

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat

/**
 * Quick Settings flashlight tile.
 *
 * Android does not allow a TileService to present runtime permission UI. If the
 * user has never granted CAMERA inside the app, the tile remains unavailable
 * until that permission is granted from one of the camera tools.
 */
class FlashlightTileService : TileService() {
    private var enabled = false

    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    override fun onClick() {
        super.onClick()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            enabled = false
            refreshTile(unavailable = true)
            return
        }

        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = runCatching {
            manager.cameraIdList.firstOrNull { id ->
                manager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        }.getOrNull()

        if (cameraId == null) {
            enabled = false
            refreshTile(unavailable = true)
            return
        }

        val next = !enabled
        if (runCatching { manager.setTorchMode(cameraId, next) }.isSuccess) {
            enabled = next
            refreshTile()
        } else {
            refreshTile(unavailable = true)
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        if (enabled) {
            val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            runCatching {
                manager.cameraIdList.firstOrNull { id ->
                    manager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                }?.let { manager.setTorchMode(it, false) }
            }
            enabled = false
        }
    }

    private fun refreshTile(unavailable: Boolean = false) {
        qsTile?.apply {
            label = "چراغ‌قوه جعبه ابزار"
            state = when {
                unavailable -> Tile.STATE_UNAVAILABLE
                enabled -> Tile.STATE_ACTIVE
                else -> Tile.STATE_INACTIVE
            }
            updateTile()
        }
    }
}
