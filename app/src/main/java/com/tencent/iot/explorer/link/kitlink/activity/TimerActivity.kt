package com.tencent.iot.explorer.link.kitlink.activity

import android.content.Intent
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.JSON
import com.tencent.iot.explorer.link.R
import com.tencent.iot.explorer.link.customview.dialog.TimerOptionsDialog
import com.tencent.iot.explorer.link.kitlink.consts.CommonField
import com.tencent.iot.explorer.link.kitlink.entity.DelayTimeExtra
import com.tencent.iot.explorer.link.kitlink.entity.ManualTask
import com.tencent.iot.explorer.link.kitlink.entity.TimerExtra
import kotlinx.android.synthetic.main.activity_delay_time.*
import kotlinx.android.synthetic.main.activity_delay_time.tv_ok
import kotlinx.android.synthetic.main.activity_delay_time.wheel_delay_time_hour
import kotlinx.android.synthetic.main.activity_delay_time.wheel_delay_time_min
import kotlinx.android.synthetic.main.activity_timer.*
import kotlinx.android.synthetic.main.menu_back_layout.*

class TimerActivity : BaseActivity() {

    private var editExtra: TimerExtra = TimerExtra()
    private var handler: Handler = Handler()

    override fun getContentView(): Int {
        return R.layout.activity_timer
    }

    override fun initView() {
        var editExtraStr = intent.getStringExtra(CommonField.EDIT_EXTRA)
        if (!TextUtils.isEmpty(editExtraStr)) {
            editExtra = JSON.parseObject(editExtraStr, TimerExtra::class.java)
        }
        tv_title.setText(R.string.dev_timer)
        initDatePicker()
        resetTimerRepeatType()
    }

    private fun resetTimerRepeatType() {
        if (editExtra.repeatType == 0) {
            tv_unset_tip_1.setText(R.string.run_one_time)
        } else if (editExtra.repeatType == 1) {
            tv_unset_tip_1.setText(R.string.everyday)
        } else if (editExtra.repeatType == 2) {
            tv_unset_tip_1.setText(R.string.work_day)
        } else if (editExtra.repeatType == 3) {
            tv_unset_tip_1.setText(R.string.weekend)
        } else if (editExtra.repeatType == 4) {
            var dayStr = ""
            for (i in 0 .. editExtra.workDays.length - 1) {
                if (editExtra.workDays.get(i).toString() == "1") {
                    when(i) {
                        0 -> {
                            dayStr += getString(R.string.sunday) + " "
                        }
                        1 -> {
                            dayStr += getString(R.string.monday) + " "
                        }
                        2 -> {
                            dayStr += getString(R.string.tuesday) + " "
                        }
                        3 -> {
                            dayStr += getString(R.string.wednesday) + " "
                        }
                        4 -> {
                            dayStr += getString(R.string.thursday) + " "
                        }
                        5 -> {
                            dayStr += getString(R.string.friday) + " "
                        }
                        6 -> {
                            dayStr += getString(R.string.saturday) + " "
                        }

                    }
                }
            }
            Log.e("XXX", "xxxxx " + dayStr)
            tv_unset_tip_1.setText(dayStr)

        }
    }

    override fun setListener() {
        iv_back.setOnClickListener { finish() }
        tv_ok.setOnClickListener {
            val intent = Intent()
            finish()
        }
        layout_repeat.setOnClickListener {
            Log.e("XXX", "editExtra \n" + JSON.toJSONString(editExtra))
            var timerOptionsDialog = TimerOptionsDialog(this@TimerActivity, editExtra)
            timerOptionsDialog.show()
            timerOptionsDialog.setOnDismisListener(onDismisListener)
        }
    }

    private var onDismisListener = object: TimerOptionsDialog.OnDismisListener {
        override fun onSaved(timerExtra: TimerExtra?) {
            if (timerExtra != null) {
                editExtra = timerExtra
                resetTimerRepeatType()
            }
        }

        override fun onCanceled() {
        }

    }

    override fun onDestroy() {
        super.onDestroy()

    }

    private fun initDatePicker() {
        var hours = ArrayList<String>()
        for (i in 0 .. 24) {
            hours.add("$i" + getString(R.string.unit_h))
        }

        var minutes = ArrayList<String>()
        for (i in 0 .. 60) {
            minutes.add("$i" + getString(R.string.unit_m))
        }
        wheel_delay_time_hour.setData(hours)
        wheel_delay_time_hour.setSelected(true)
        wheel_delay_time_min.setData(minutes)
        wheel_delay_time_min.setSelected(true)

            handler.postDelayed( {
                wheel_delay_time_hour.setSelectedItemPosition(editExtra.hours, false)
                wheel_delay_time_min.setSelectedItemPosition(editExtra.minute, false)
            }, 10)

        wheel_delay_time_hour.setIndicator(true)
        wheel_delay_time_hour.setAtmospheric(true)
        wheel_delay_time_min.setIndicator(true)
        wheel_delay_time_min.setAtmospheric(true)
    }
}