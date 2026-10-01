package com.selfuse.phoneassist

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * 커스텀 타이머 화면.
 * - 초기화 버튼: 표시 시간을 00:00:00으로 되돌림
 * - -15분 / -5분 / +5분 / +15분 버튼: 설정 시간(또는 카운트다운 중인 남은 시간)을 즉시 증감
 * - 시작/일시정지, 정지 버튼으로 실제 카운트다운 제어
 */
class TimerActivity : AppCompatActivity() {

    private lateinit var timeDisplay: TextView
    private lateinit var startPauseButton: Button

    private var remainingMillis: Long = 0L
    private var isRunning: Boolean = false
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        timeDisplay = findViewById(R.id.timeDisplay)
        startPauseButton = findViewById(R.id.startPauseButton)
        val minus15Button: Button = findViewById(R.id.minus15Button)
        val minus5Button: Button = findViewById(R.id.minus5Button)
        val add5Button: Button = findViewById(R.id.add5Button)
        val add15Button: Button = findViewById(R.id.add15Button)
        val resetButton: Button = findViewById(R.id.resetButton)
        val stopButton: Button = findViewById(R.id.stopButton)

        updateDisplay()

        minus15Button.setOnClickListener { addMinutes(-15) }
        minus5Button.setOnClickListener { addMinutes(-5) }
        add5Button.setOnClickListener { addMinutes(5) }
        add15Button.setOnClickListener { addMinutes(15) }
        resetButton.setOnClickListener { resetTimer() }
        stopButton.setOnClickListener { resetTimer() }

        startPauseButton.setOnClickListener {
            if (isRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }
    }

    /**
     * 남은 시간에 분 단위를 더하거나 뺌 (음수 전달 시 감소).
     * 0 미만으로는 내려가지 않도록 하한을 0으로 고정함.
     * 실행 중이면 새 남은 시간으로 카운트다운을 재시작하되,
     * 감소로 인해 0에 도달한 경우 정상 종료와 동일하게 처리함.
     */
    private fun addMinutes(minutes: Int) {
        val target = remainingMillis + minutes * 60_000L
        remainingMillis = target.coerceAtLeast(0L)
        updateDisplay()
        if (isRunning) {
            countDownTimer?.cancel()
            if (remainingMillis <= 0L) {
                onTimerFinished()
            } else {
                startCountDown(remainingMillis)
            }
        }
    }

    /** 타이머 종료 시 공통 처리 (정상 완주 시 / 실행 중 감소로 0에 도달한 경우 공용). */
    private fun onTimerFinished() {
        countDownTimer?.cancel()
        countDownTimer = null
        remainingMillis = 0L
        isRunning = false
        startPauseButton.text = "시작"
        updateDisplay()
        Toast.makeText(this, "타이머 종료", Toast.LENGTH_SHORT).show()
    }

    /** 00:00:00으로 초기화. 실행 중이던 카운트다운도 중단함. */
    private fun resetTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        remainingMillis = 0L
        isRunning = false
        startPauseButton.text = "시작"
        updateDisplay()
    }

    private fun startTimer() {
        if (remainingMillis <= 0L) {
            Toast.makeText(this, "먼저 +5분 / +15분으로 시간을 추가하세요", Toast.LENGTH_SHORT).show()
            return
        }
        isRunning = true
        startPauseButton.text = "일시정지"
        startCountDown(remainingMillis)
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        isRunning = false
        startPauseButton.text = "시작"
    }

    private fun startCountDown(fromMillis: Long) {
        countDownTimer = object : CountDownTimer(fromMillis, 1_000L) {
            override fun onTick(msLeft: Long) {
                remainingMillis = msLeft
                updateDisplay()
            }

            override fun onFinish() {
                onTimerFinished()
            }
        }.start()
    }

    private fun updateDisplay() {
        val totalSec = remainingMillis / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        timeDisplay.text = String.format("%02d:%02d:%02d", h, m, s)
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}
