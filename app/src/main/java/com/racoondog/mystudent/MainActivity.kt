package com.racoondog.mystudent

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.racoondog.mystudent.databinding.ItemNameBinding
import kotlinx.android.synthetic.main.activity_main.*
import me.grantland.widget.AutofitTextView
import android.content.DialogInterface
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.createschedule_layout.*


class MainActivity: AppCompatActivity() {

    val memo = arrayListOf<Title>()
    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(my_toolbar)
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 알림창 객체 생성

        title_view.apply {
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = TitleAdapter(memo){

                val builder = AlertDialog.Builder(this@MainActivity)
                val dialog = builder.create()
                builder.setTitle("메모 삭제")        // 제목 설정
                    .setMessage("메모를 삭제하시겠습니까?")        // 메세지 설정
                    .setCancelable(true)        // 뒤로 버튼 클릭시 취소 가능 설정
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, whichButton ->

                        memo.remove(Title("${it.name}"))
                        adapter?.notifyDataSetChanged()
                        Toast.makeText(this@MainActivity,"메모가 삭제되었습니다.",Toast.LENGTH_SHORT).show()
                    })

                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, whichButton ->

                        //Toast.makeText(this@MainActivity,"취소되었습니다.",Toast.LENGTH_SHORT).show()
                        // 취소 버튼 클릭시 설정, 왼쪽 버튼입니다.
                        //원하는 클릭 이벤트를 넣으시면 됩니다.
                    })
                dialog.show()

            }
        }

        open_list.setOnClickListener{
            title_bar.visibility = View.VISIBLE
            open_list.visibility = View.INVISIBLE
            close_list.visibility = View.VISIBLE
        }
        title_text.setOnClickListener{

        }
        close_list.setOnClickListener{
            title_bar.visibility = View.INVISIBLE
            open_list.visibility = View.VISIBLE
            close_list.visibility = View.INVISIBLE
        }


        memo_add.setOnClickListener{
            val MemoIntent = Intent(this, CreateMemo::class.java)
            startActivityForResult(MemoIntent,101)
        }

        schedule_add.setOnClickListener{
            val ScheduleIntent = Intent(this, CreateSchedule::class.java)
            startActivityForResult(ScheduleIntent, 100)
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                100 -> {
                    title_text.text = data!!.getStringExtra("title")
                    val dayflag = data.getIntExtra("day_flag",0)
                    val start_time = data.getIntExtra("start_time",0)
                    val end_time = data.getIntExtra("end_time",0)
                    LoadSchedule(dayflag,start_time,end_time)
                }
                101 ->{
                    memo.add(Title(data!!.getStringExtra("memo").toString()+",  "))
                }
            }
        }
    }

    fun  LoadSchedule(day_flag : Int, start_time : Int, end_time : Int) {

        var day = listOf<String>()

        var period = mutableListOf<String>()

        var time = mutableListOf<String>()

        var subject = listOf("화1","화2")

        var content = listOf("태경이삼촌과 레슨")

        if (day_flag == 1){
            day = listOf("월","화","수","목","금")
        }
        else if (day_flag == 2){
            day = listOf("  월","화","수","목","금","토")
        }
        else if(day_flag == 3){
            day = listOf("   월","화","수","목","금","토","일")
        } // day_line 레이아웃 문제로 띄어쓰기함 늘리거나 줄일수록 위에 날짜 사이즈 변함

        var day_flag = 0

        for (i in start_time..end_time){
            if (i == start_time && i < 10) {
                period.add("$i\n 오전 ")
            }
            else if (i == start_time && i < 12 && i >= 10) {
                period.add("$i\n 오전 ")
            }
            else if (i == start_time && i > 12) {
                period.add("${i-12}\n 오후 ")
                day_flag =1
            }
            else if(i ==13){
                period.add("${i-12}\n 오후 ")
                day_flag = 1
            }
            else if (day_flag == 1){
                period.add("${i-12}")
            }
            else {
                period.add("$i")
            }
        }

        /*
        for (i in 1..end_time - start_time) {

            period.add("$i")
        }*/
        //교시 ex ) 1,2,3

        /*

        var day_flag = 0

        for (i in start_time..end_time){
            if (i == start_time && i < 10) {
                time.add(" 오전\n $i" + ":00 ")
            }
            else if (i == start_time && i < 12 && i >= 10) {
                time.add("  오전\n$i" + ":00 ")
            }
            else if (i == start_time && i > 12) {
                time.add(" 오후\n ${i-12}" + ":00 ")
                day_flag =1
            }
            else if(i ==13){
                time.add(" 오후\n ${i-12}" + ":00 ")
                day_flag = 1
            }
            else if (day_flag == 1){
                time.add(" ${i-12}" + ":00 ")
            }
            else {
                time.add(" $i" + ":00 ")
            }
        }*/
        //period 밑에 작은 오전/오후 시간 표시 ex)오후 1:00 -> time 으로 정의 밑에 레이아웃도 세팅해야함

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

            if(day_flag == 1){
                initday.text = "  "
            }
            if(day_flag == 2) {
                initday.text = "     "
            }
            if(day_flag == 3) {
                initday.text = "      "
            }
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

        Day_Line.addView(dayrow)

        for (i in 0 until period.size) {



            val timerow = TableRow(this)
            timerow.layoutParams  = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT).apply {
                weight =1f
            }
            timerow.setBackgroundResource(R.color.whitegray_bg)

            val const_init = ConstraintLayout(this)
            const_init.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT).apply{
                weight - 1f
            }

            val initperiod = TextView(this)
            initperiod.gravity = Gravity.CENTER
            initperiod.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                initperiod.text = period[i]
                initperiod.textSize = 13f
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
                leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                //verticalBias = 0.2f
                //time 사용시 period 레이아웃 영역 활성화
            }
            /*
            val inittime = TextView(this)
            inittime.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
                inittime.text = time[i]
                inittime.textSize = 10f
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
                leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                verticalBias = 0.8f
            }

            const_init.addView(inittime)
             */ //time 레이아웃 세팅 영역 ()

            const_init.addView(initperiod)
            timerow.addView(const_init)

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
class TitleAdapter(val items :List<Title>, private val clickListener: (title:Title) ->Unit) : RecyclerView.Adapter<TitleAdapter.TitleViewHolder>(){

    class TitleViewHolder(val binding : ItemNameBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TitleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_name,parent,false)
        val viewHolder = TitleViewHolder(ItemNameBinding.bind(view))
        view.setOnClickListener{
            clickListener.invoke(items[viewHolder.adapterPosition])
        }
        return viewHolder
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: TitleViewHolder, position: Int) {
        holder.binding.title = items[position]
    }

}