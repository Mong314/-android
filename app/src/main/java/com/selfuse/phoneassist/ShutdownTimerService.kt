package com.selfuse.phoneassist

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * 지정한 시간이 지나면 기기를 끄는 것을 시도하는 포그라운드 서비스.
 *
 * 동작 순서:
 * 1) 루트(su)가 있으면 `reboot -p` 로 실제 전원 종료를 시도한다.
 * 2) 루트가 없거나 실패하면, 기기 관리자 권한이 등록되어 있을 경우 화면을 잠근다 (lockNow).
 * 3) 그것도 안 되면 알림으로 사용자에게 수동 종료를 안내한다.
 *
 * 루트 권한이 없는 일반 기기에서는 1)이 항상 실패하는 것이 정상이며,
 * 이는 안드로이드 OS 정책상 제약이지 이 앱의 버그가 아니다.
 */
class ShutdownTimerService : Service() {

    private var timer: CountDownTimer? = null

    companion object {
        const val EXTRA_MINUTES = "extra_minutes"
        const val CHANNEL_ID = "shutdown_timer_channel"
        const val NOTIF_ID = 1
        const val ACTION_CANCEL = "com.selfuse.phoneassist.ACTION_CANCEL"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL) {
            stopSelfCleanly()
            return START_NOT_STICKY
        }

        val minutes = intent?.getIntExtra(EXTRA_MINUTES, 0) ?: 0
        if (minutes <= 0) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIF_ID, buildNotification("종료까지 $minutes 분 남음"))

        val totalMs = minutes * 60_000L
        timer = object : CountDownTimer(totalMs, 1_000L) {
            override fun onTick(msLeft: Long) {
                val minLeft = (msLeft / 60_000L) + 1
                updateNotification("종료까지 약 ${minLeft}분 남음")
            }

            override fun onFinish() {
                attemptShutdown()
            }
        }.start()

        return START_STICKY
    }

    private fun attemptShutdown() {
        val rootSucceeded = tryRootShutdown()
        if (rootSucceeded) {
            return
        }
        val locked = tryDeviceAdminLock()
        if (!locked) {
            updateNotification("자동 종료 실패: 루트/관리자 권한 없음. 수동으로 꺼주세요.")
        }
        stopSelfCleanly()
    }

    /** su를 통한 실제 전원 종료 시도. 루트가 없는 기기에서는 항상 실패한다. */
    private fun tryRootShutdown(): Boolean {
        return try {
            val process = ProcessBuilder("su").redirectErrorStream(true).start()
            val out = process.outputStream
            out.write("reboot -p\n".toByteArray())
            out.write("exit\n".toByteArray())
            out.flush()
            out.close()
            val exit = process.waitFor()
            exit == 0
        } catch (e: Exception) {
            false
        }
    }

    /** 루트가 없을 때의 대비책: 기기 관리자 권한이 등록돼 있으면 화면을 잠근다. */
    private fun tryDeviceAdminLock(): Boolean {
        return try {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin = ComponentName(this, DeviceAdminReceiver::class.java)
            if (dpm.isAdminActive(admin)) {
                dpm.lockNow()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun stopSelfCleanly() {
        timer?.cancel()
        timer = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(text: String): android.app.Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "종료 타이머", NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        val cancelIntent = Intent(this, ShutdownTimerService::class.java).apply {
            action = ACTION_CANCEL
        }
        val cancelPending = PendingIntent.getService(
            this, 0, cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Phone Assist")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_power_off)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "취소", cancelPending)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID, buildNotification(text))
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
