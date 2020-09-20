package com.racoondog.gotaplan

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService

/**
 * 런처 앱에 리스트뷰의 어뎁터 역할을 해주는 클래스
 */
class MyRemoteViewsFactory(context: Context?) : RemoteViewsService.RemoteViewsFactory {
    //context 설정하기
    var context: Context? = null
    var arrayList: ArrayList<WidgetItem>? = null

    //DB를 대신하여 arrayList에 데이터를 추가하는 함수ㅋㅋ
    fun setData() {
        arrayList = ArrayList()
        arrayList!!.add(WidgetItem(1, "First"))
        arrayList!!.add(WidgetItem(2, "Second"))
        arrayList!!.add(WidgetItem(3, "Third"))
        arrayList!!.add(WidgetItem(4, "Fourth"))
        arrayList!!.add(WidgetItem(5, "Fifth"))
    }

    //이 모든게 필수 오버라이드 메소드
    //실행 최초로 호출되는 함수
    override fun onCreate() {
        setData()
    }

    //항목 추가 및 제거 등 데이터 변경이 발생했을 때 호출되는 함수
    //브로드캐스트 리시버에서 notifyAppWidgetViewDataChanged()가 호출 될 때 자동 호출
    override fun onDataSetChanged() {
        setData()
    }

    //마지막에 호출되는 함수
    override fun onDestroy() {}

    // 항목 개수를 반환하는 함수
    override fun getCount(): Int {
        return arrayList!!.size
    }

    //각 항목을 구현하기 위해 호출, 매개변수 값을 참조하여 각 항목을 구성하기위한 로직이 담긴다.
    // 항목 선택 이벤트 발생 시 인텐트에 담겨야 할 항목 데이터를 추가해주어야 하는 함수
    override fun getViewAt(position: Int): RemoteViews {
        val listviewWidget = RemoteViews(context?.getPackageName(), R.layout.widget_item_collection)
        listviewWidget.setTextViewText(R.id.text1, arrayList!![position].content)

        // 항목 선택 이벤트 발생 시 인텐트에 담겨야 할 항목 데이터를 추가해주는 코드
        val dataIntent = Intent()
        dataIntent.putExtra("item_id", arrayList!![position].get_id())
        dataIntent.putExtra("item_data", arrayList!![position].content)
        listviewWidget.setOnClickFillInIntent(R.id.text1, dataIntent)
        //setOnClickFillInIntent 브로드캐스트 리시버에서 항목 선택 이벤트가 발생할 때 실행을 의뢰한 인텐트에 각 항목의 데이터를 추가해주는 함수
        //브로드캐스트 리시버의 인텐트와 Extra 데이터가 담긴 인텐트를 함치는 역할을 한다.
        return listviewWidget
    }

    //로딩 뷰를 표현하기 위해 호출, 없으면 null
    override fun getLoadingView(): RemoteViews? {
        return null
    }

    //항목의 타입 갯수를 판단하기 위해 호출, 모든 항목이 같은 뷰 타입이라면 1을 반환하면 된다.
    override fun getViewTypeCount(): Int {
        return 1
    }

    //각 항목의 식별자 값을 얻기 위해 호출
    override fun getItemId(position: Int): Long {
        return 0
    }

    // 같은 ID가 항상 같은 개체를 참조하면 true 반환하는 함수
    override fun hasStableIds(): Boolean {
        return false
    }

    init {
        this.context = context
    }
}
