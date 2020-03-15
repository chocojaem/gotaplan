package com.racoondog.gotaplan

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.schedule_time_dialog.*

class ScheduleTimeDialog: Dialog {

    lateinit var cnxt:ScheduleDialog

    private val realm = Realm.getDefaultInstance()

    constructor(context: Context) : super(context)
    constructor(context: Context, themeResId: Int) : super(context, themeResId)
    constructor(
        context: Context,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(context, cancelable, cancelListener)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        layoutParams.dimAmount = 0.8f
        window!!.attributes = layoutParams
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.schedule_time_dialog)

        val scheduleData = realm.where(ScheduleData::class.java).findFirst()!!
        val subjectData: RealmResults<SubjectData> = realm.where<SubjectData>(SubjectData::class.java).findAll()

        var dayFlag = scheduleData.scheduleDayFlag
        when(dayFlag){
                5 -> Friday.isChecked = true
                6 -> Saturday.isChecked = true
                7 -> Sunday.isChecked = true
            }
        Friday.setOnClickListener {
            dayFlag = 5

        }
        Saturday.setOnClickListener {
            dayFlag = 6

        }
        Sunday.setOnClickListener {
            dayFlag = 7

        }

        schedule_time_dialog_start_time.setText(scheduleData.scheduleStartHour.toString())
        schedule_time_dialog_end_time.setText(scheduleData.scheduleEndHour.toString())
        schedule_time_dialog_start_time.hint = scheduleData.scheduleStartHour.toString()
        schedule_time_dialog_end_time.hint = scheduleData.scheduleEndHour.toString()

        schedule_time_dialog_apply.setOnClickListener {

            if (!(schedule_time_dialog_start_time.text.toString() == "" || schedule_time_dialog_end_time.text.toString() == "")) {
                when {
                    schedule_time_dialog_start_time.text.toString().toInt() < 6 ->
                        Toast.makeText(context,R.string.schedule_time_dialog_start_min,Toast.LENGTH_SHORT).show()
                    schedule_time_dialog_start_time.text.toString().toInt() > 24 ->
                        Toast.makeText(context,R.string.schedule_time_dialog_start_max,Toast.LENGTH_SHORT).show()
                    schedule_time_dialog_end_time.text.toString().toInt() < 6 ->
                        Toast.makeText(context, R.string.schedule_time_dialog_end_min, Toast.LENGTH_SHORT).show()
                    schedule_time_dialog_end_time.text.toString().toInt() > 24 ->
                        Toast.makeText(context, R.string.schedule_time_dialog_end_max, Toast.LENGTH_SHORT).show()
                    schedule_time_dialog_start_time.text.toString().toInt() > schedule_time_dialog_end_time.text.toString().toInt() ->
                        Toast.makeText(context, R.string.startTime_big, Toast.LENGTH_SHORT).show()
                    schedule_time_dialog_start_time.text.toString().toInt() == schedule_time_dialog_end_time.text.toString().toInt() ->
                        Toast.makeText(context, R.string.startTime_equal, Toast.LENGTH_SHORT).show()
                    else -> {

                        val startTimeData = subjectData.sort("startHour", Sort.ASCENDING)
                        val endTimeData = subjectData.sort("endHour", Sort.DESCENDING)
                        if (subjectData.toString() == "[]"){
                            realm.beginTransaction()
                            scheduleData.scheduleStartHour = schedule_time_dialog_start_time.text.toString().toInt()
                            scheduleData.scheduleEndHour = schedule_time_dialog_end_time.text.toString().toInt()
                            scheduleData.scheduleDayFlag = dayFlag
                            realm.commitTransaction()
                            cnxt.cnxt.weekView.refresh(cnxt.cnxt.weekView)
                            dismiss()
                            cnxt.dismiss()
                        }else{

                            when {
                                startTimeData[0]!!.startHour >= schedule_time_dialog_start_time.text.toString().toInt() -> {

                                    when {
                                        endTimeData[0]!!.endHour < schedule_time_dialog_end_time.text.toString().toInt() -> {

                                            val dayFlagData = subjectData.sort("dayFlag", Sort.DESCENDING)

                                            if(dayFlag >= dayFlagData[0]!!.dayFlag){

                                                realm.beginTransaction()
                                                scheduleData.scheduleStartHour = schedule_time_dialog_start_time.text.toString().toInt()
                                                scheduleData.scheduleEndHour = schedule_time_dialog_end_time.text.toString().toInt()
                                                scheduleData.scheduleDayFlag = dayFlag
                                                realm.commitTransaction()
                                                cnxt.cnxt.weekView.refresh(cnxt.cnxt.weekView)
                                                dismiss()
                                                cnxt.dismiss()

                                            }else{
                                                Toast.makeText(context, R.string.schedule_time_dialog_subject_exist_day, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        endTimeData[0]!!.endHour == schedule_time_dialog_end_time.text.toString().toInt() -> {
                                            if(endTimeData[0]!!.endMinute.toInt() == 0){

                                                val dayFlagData = subjectData.sort("dayFlag", Sort.DESCENDING)

                                                if(dayFlag >= dayFlagData[0]!!.dayFlag){

                                                    realm.beginTransaction()
                                                    scheduleData.scheduleStartHour = schedule_time_dialog_start_time.text.toString().toInt()
                                                    scheduleData.scheduleEndHour = schedule_time_dialog_end_time.text.toString().toInt()
                                                    scheduleData.scheduleDayFlag = dayFlag
                                                    realm.commitTransaction()
                                                    cnxt.cnxt.weekView.refresh(cnxt.cnxt.weekView)
                                                    dismiss()
                                                    cnxt.dismiss()

                                                }else{
                                                    Toast.makeText(context, R.string.schedule_time_dialog_subject_exist_day, Toast.LENGTH_SHORT).show()
                                                }

                                            }
                                            else{
                                                Toast.makeText(context, R.string.schedule_time_dialog_subject_exist_time, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        else -> Toast.makeText(context, R.string.schedule_time_dialog_subject_exist_time, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                else -> Toast.makeText(context, R.string.schedule_time_dialog_subject_exist_time, Toast.LENGTH_SHORT).show()
                            }

                        }

                    }
                }



            }


        }
        schedule_time_dialog_cancel.setOnClickListener{
            dismiss()
        }


    }

}