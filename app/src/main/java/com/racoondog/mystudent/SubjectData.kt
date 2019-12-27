package com.racoondog.mystudent

/*object ScheduleData{

   var Title:String? = null
   var ScheduleDayFlag:Int? = null
   var ScheduleStartHour:Int? = null
   var ScheduleEndHour:Int? = null

}
 */

object SubjectData {

    var SubjectInfo = mutableListOf<Array<Any>?>()
    val LessonInfo = mutableListOf<Any>()

    var id: Int = 0

    var StartHour: Int = 0
    var StartMinute: Int = 0

    var EndHour: Int = 0
    var EndMinute: Int = 0

    lateinit var TitleText: String
    lateinit var ContentText: String
    lateinit var TimeText: String


    fun getData(ID: Int): Any {
        id = ID
        return SubjectInfo[ID]!!.contentDeepToString()

    }

    fun setData(ID: Int) {

        id = ID
        val dataInfo = arrayOf(id, StartHour, StartMinute, EndHour, EndMinute, TitleText, ContentText, TimeText)

        SubjectInfo.add(dataInfo)



    }

    fun setTitle(Title:String){
        SubjectInfo!![id]!![0] = Title
    }
    fun setContent(Content:String){
        SubjectInfo!![id]!![3] = Content
    }

}
