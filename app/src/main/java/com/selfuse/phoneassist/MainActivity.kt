package com.selfuse.phoneassist

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var minutesInput: EditText
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        minutesInput = findViewById(R.id.minutesInput)
        statusText = findViewById(R.id.statusText)
        val startButton: Button = findViewById(R.id.startButton)
        val cancelButton: Button = findViewById(R.id.cancelButton)
        val requestAdminButton: Button = findViewById(R.id.requestAdminButton)
        val openTimerButton: Button = findViewById(R.id.openTimerButton)

        openTimerButton.setOnClickListener {
            startActivity(Intent(this, TimerActivity::class.java))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100
            )
        }

        startButton.setOnClickListener {
            val minutes = minutesInput.text.toString().toIntOrNull()
            if (minutes == null || minutes <= 0) {
                Toast.makeText(this, "분을 올바르게 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, ShutdownTimerService::class.java).apply {
                putExtra(ShutdownTimerService.EXTRA_MINUTES, minutes)
            }
            ContextCompat.startForegroundService(this, intent)
            statusText.text = "$minutes 분 후 종료를 시도합니다"
        }

        cancelButton.setOnClickListener {
            val intent = Intent(this, ShutdownTimerService::class.java).apply {
                action = ShutdownTimerService.ACTION_CANCEL
            }
            startService(intent)
            statusText.text = "취소됨"
        }

        requestAdminButton.setOnClickListener {
            val admin = ComponentName(this, DeviceAdminReceiver::class.java)
            val intent = Intent(android.app.admin.DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                putExtra(
                    android.app.admin.DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "루트가 없을 때 화면을 강제로 잠그기 위한 권한입니다 (전원 종료는 아님)."
                )
            }
            startActivity(intent)
        }
    }
}
