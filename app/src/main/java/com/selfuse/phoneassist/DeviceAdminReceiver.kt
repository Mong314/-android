package com.selfuse.phoneassist

import android.app.admin.DeviceAdminReceiver as AndroidDeviceAdminReceiver
import android.content.Context
import android.content.Intent

/**
 * 루트가 없는 기기에서 완전 종료가 불가능할 때의 대비책(fallback).
 * 이 리시버를 기기 관리자로 등록하면 서비스가 lockNow()를 호출해
 * 화면을 즉시 잠글 수 있음 (전원 종료는 아님).
 */
class DeviceAdminReceiver : AndroidDeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }
}
