package com.racoondog.mystudent

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.schedule_layout.*
import me.grantland.widget.AutofitTextView


class MainActivity: AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(my_toolbar)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val day = listOf("월","화","수","목","금")
        val time = listOf("8","9","10","11","12","1","2","3","4","5","6","7","8")
        val subject = listOf("화1","화2")
        val content = listOf("태경이삼촌과 레슨")

        val layout = TableLayout(this)

        val dayrow = TableRow(this)

        layout.layoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT).apply {

        }

        dayrow.layoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT).apply {

        }

        val initday = TextView(this)
        initday.setBackgroundColor(Color.RED)
        initday.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT).apply {
            initday.text =" "
            weight = 1f
        }

        dayrow.addView(initday)


        for (i in 0 until day.size) {

            val daytxt = TextView(this)
            daytxt.gravity = Gravity.CENTER
            daytxt.setBackgroundResource(R.color.Actionbar_bg)
            daytxt.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT).apply{
                daytxt.text = day[i]
                weight = 3f

            }

            dayrow.addView(daytxt)
        }

        layout.addView(dayrow)

        for (i in 0 until time.size) {



            val timerow = TableRow(this)
            timerow.layoutParams  = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT).apply {
                weight =1f
            }
            timerow.setBackgroundResource(R.color.whitegray_bg)

            val inittime = TextView(this)
            inittime.gravity = Gravity.CENTER
            inittime.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            ).apply {
                inittime.text = time[i]
                weight = 1f
                gravity = Gravity.CENTER
                width = 0

            }
            timerow.addView(inittime)

            for (j in 0 until day.size) {

                val timetxt =  AutofitTextView(this)
                val tag  : String = day[j] + i
                timetxt.tag = tag
                timetxt.setBackgroundResource(R.drawable.cell_shape)
                timetxt.maxLines = 2
                timetxt.textSize = 40f
                timetxt.setMinTextSize(10)

                for (k in 0 until subject.size) {
                    if (timetxt.tag == subject[k]) {
                        timetxt.setBackgroundColor(Color.LTGRAY)
                    }
                }
                if(timetxt.tag == subject[0]){
                    timetxt.text = content[0]
                }


                timetxt.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                ).apply {
                    height = 150
                    width = 0
                    weight = 3f

                }

                timerow.addView(timetxt)

            }



            layout.addView(timerow)
        }

        scheduleview.addView(layout)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.home -> {
                //onBackPressed()
                return true
            }
            R.id.setting -> {
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
}
