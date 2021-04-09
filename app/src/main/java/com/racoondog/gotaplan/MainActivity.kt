package com.racoondog.gotaplan

import android.app.Activity
import android.app.AlertDialog.BUTTON_POSITIVE
import android.app.AlertDialog.Builder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.billingclient.api.*
import com.android.billingclient.api.SkuDetailsResponseListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity: AppCompatActivity(),PurchasesUpdatedListener{

    //Developer: Void

    private val realm = Realm.getDefaultInstance()

    companion object{
        var mContext:Context? = null
        var scheduleID: Int = 0 // 메인화면에 보여지는 시간표의 id

    }
    val weekView by lazy { WeekView(this) }

    private var intentStartTime: Int = 0
    private var intentEndTime: Int = 0
    private var intentFlag: Int = 0
    private val storage:AppStorage by lazy { AppStorage(this) }
    private lateinit var mInterstitialAd: InterstitialAd

    private var billingClient: BillingClient? = null
    private var skuDetail:SkuDetails? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = this
        setSupportActionBar(main_toolbar)  //Actionbar 부분
        supportActionBar?.setDisplayUseLogoEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        loadData()//데이터 불러오기

        if (!storage.purchasedRemoveAds() && !storage.showHelpView()) {
            MobileAds.initialize(this, getString(R.string.ad_mob_app_id))
            mInterstitialAd = InterstitialAd(this)
            mInterstitialAd.adUnitId = getString(R.string.front_ad_unit_id)
            mInterstitialAd.loadAd(AdRequest.Builder().build())
            mInterstitialAd.adListener = object: AdListener() { //전면 광고의 상태를 확인하는 리스너 등록
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    mInterstitialAd.show()

                }
            }


            // Set up the billing client
            billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build()
            billingClient!!.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.e("Tag", "구글 결제 서버 접속에 성공했습니다.")
                        queryOneTimeProducts()
                    } else {

                        Log.e("Tag", "구글 결제 서버 접속에 실패하였습니다.\n" + "오류코드: ${billingResult.responseCode}")

                        // case 구글 플레이스토어 계정 정보 인식 안될 때

                    }
                }

                override fun onBillingServiceDisconnected() {
                    Toast.makeText(
                        this@MainActivity, "구글 결제 서버와 접속이 끊어졌습니다.\n" +
                                "오류코드", Toast.LENGTH_LONG
                    ).show()
                }
            })

        }

        showHelpView()// 앱 가이드 보여줌

        weekView_layout.setOnClickListener {
            val scheduleData = realm.where(ScheduleData::class.java).findFirst()

            if (scheduleData == null) {
                val scheduleIntent = Intent(this, CreateSchedule::class.java)
                scheduleIntent.flags = (Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivityForResult(scheduleIntent, 100)
            }

        }

        addSubjectButton.setOnClickListener {
            val subjectIntent = Intent(this, CreateSubject::class.java)
            subjectIntent.flags = (Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivityForResult(subjectIntent, 102)
        }

        side_menu_btn.setOnClickListener {
            // 나중에 시간표 여러개 생성될때 side_menu_btn visible 시키고 고쳐쓰면됨
            val sideMenu = SideMenu(this)
            sideMenu.addSideView(main, fl_silde, view_sildebar)
            sideMenu.showMenu(main, fl_silde, view_sildebar)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

            super.onActivityResult(requestCode, resultCode, data)
            //MainActivity 로 들어오는 onActivityResult 부분 -> Intent 후 값 반환
            if (resultCode == Activity.RESULT_OK) {

                when (requestCode) {
                    100 -> {


                        val scheduleDayFlag = data!!.getIntExtra("scheduleDayFlag", 0)
                        val scheduleStartHour = data.getIntExtra("scheduleStartHour", 0)
                        val scheduleEndHour = data.getIntExtra("scheduleEndHour", 0)

                        schedule_add.visibility = View.INVISIBLE
                        addSubjectButton.visibility = View.VISIBLE

                        toolbar_title.text = data.getStringExtra("title")

                        intentStartTime = scheduleStartHour
                        intentEndTime = scheduleEndHour
                        intentFlag = scheduleDayFlag

                        realm.beginTransaction()
                        val dataBase: ScheduleData = realm.createObject(ScheduleData::class.java)

                        dataBase.apply {
                            this.dayFlag = scheduleDayFlag
                            this.startHour = scheduleStartHour
                            this.endHour = scheduleEndHour
                            this.title = toolbar_title.text.toString()
                        }
                        realm.commitTransaction()

                        weekView.layoutParams = ConstraintLayout.LayoutParams(
                            ConstraintLayout.LayoutParams.MATCH_PARENT,
                            ConstraintLayout.LayoutParams.MATCH_PARENT
                        ).apply {

                        }

                        weekView.drawSchedule(scheduleDayFlag, scheduleStartHour, scheduleEndHour)

                        weekView_layout.addView(weekView)
                        main_text.visibility = View.INVISIBLE

                    }
                    101 -> {

                    }
                    102 -> {
                        weekView.refresh(weekView)

                    }
                    103 -> {
                        weekView.refresh(weekView)

                    }
                    105 -> {
                        val statusBarColor = data!!.getIntExtra("statusBarColor", 0)
                        val mainButtonBarColor = data.getIntExtra("mainButtonColor", 0)

                        val themeData = realm.where(ThemeData::class.java).findFirst()!!
                        realm.beginTransaction()
                        themeData.statusBarColor = statusBarColor
                        themeData.mainButtonColor = mainButtonBarColor
                        realm.commitTransaction()

                    }

                }
            }
            if (resultCode == 104) {

                when (requestCode) {

                    103 -> {
                        weekView.deleteSubject(WeekView.ID)


                    }

                }

            }


    }

    private fun showHelpView(){
        if(storage.showHelpView()){
            val introIntent = Intent(this, IntroActivity::class.java)
            introIntent.action = "TimetableGuide"
            startActivity(introIntent)
            storage.setHelpView(false)
        }
    }


    private fun loadData() {

        val scheduleData = realm.where(ScheduleData::class.java).findFirst()
        if (scheduleData != null) {

            main_text.visibility = View.INVISIBLE
            schedule_add.visibility = View.INVISIBLE
            addSubjectButton.visibility = View.VISIBLE
            if(scheduleData?.title != "") {
                toolbar_title?.text = scheduleData?.title
            }

            intentFlag = scheduleData.dayFlag
            intentStartTime = scheduleData.startHour
            intentEndTime = scheduleData.endHour

            weekView.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            ).apply {

            }
            weekView.drawSchedule(intentFlag, intentStartTime, intentEndTime)
            weekView_layout.addView(weekView)


            val subjectData: RealmResults<SubjectData> =
                realm.where<SubjectData>(SubjectData::class.java).findAll()
            for (data in subjectData) {
                weekView.createSubject(
                    data.startHour,
                    data.startMinute.toInt(),
                    data.endHour,
                    data.endMinute.toInt(),
                    data.dayFlag,
                    scheduleData.startHour,
                    data.id,
                    data.subjectColor
                )
            }
            weekView.updateWidget()
        }


    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean { //Menu 추가 부분
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { //Menu 목록 부분
        val scheduleData = realm.where(ScheduleData::class.java).findFirst()
        when (item.itemId) {
            R.id.home -> {
                //onBackPressed()
                return true
            }
            R.id.scheduleSetting -> {
                if (scheduleData == null) {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.add_schedule_first),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val dialog = ScheduleDialog(this)
                    dialog.cnxt = this
                    dialog.show()

                }
                return true
            }
            R.id.subjectSetting -> {
                if (scheduleData == null) {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.add_schedule_first),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val dialog = SubjectDialog(this)
                    dialog.cnxt = this
                    dialog.show()
                }
                return true
            }
            R.id.purchasePro -> {
                purchase(skuDetail)
                return true
            }

            R.id.help -> {
                val introIntent = Intent(this, IntroActivity::class.java)
                introIntent.action = "TimetableGuide"
                startActivity(introIntent)
                return true
            }
            R.id.directFeedback -> {
                val directFeedbackIntent = Intent(this, DirectFeedback::class.java)
                startActivity(directFeedbackIntent)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()

    }

    fun checkPermissions(permission: String, result: () -> Unit){

        //거절되었거나 아직 수락하지 않은 권한(퍼미션)을 저장할 문자열 배열 리스트
        //필요한 퍼미션들을 하나씩 끄집어내서 현재 권한을 받았는지 체크
        val rejectedPermissionList = ArrayList<String>()

        if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            //만약 권한이 없다면 rejectedPermissionList 로 추가
            rejectedPermissionList.add(permission)

        }
        else{
            result()
        }

        //거절된 퍼미션이 있다면...
        if(permission in rejectedPermissionList){
            //권한 요청
            val array = arrayOfNulls<String>(rejectedPermissionList.size)
            ActivityCompat.requestPermissions(this, rejectedPermissionList.toArray(array), 100)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            100 -> {
                if (grantResults.isNotEmpty()) {
                    for ((i, permission) in permissions.withIndex()) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {

                            //권한 획득 실패

                            val dialog = Builder(this).apply {
                                val n: String = Locale.getDefault().displayLanguage
                                if (n.compareTo("한국어") == 0) {
                                    this.setMessage("다음 기능을 사용하기 위해서는 $permission 권한이 필요합니다. 계속 하시겠습니까?")
                                } else {
                                    this.setMessage("$permission permission is required to use the following features: Would you like to go on?")
                                }
                            }
                                .setCancelable(false)
                                .setPositiveButton(resources.getString(R.string.dialog_apply)) { _, _ ->
                                    ActivityCompat.requestPermissions(this, permissions, 100)
                                }
                                .show()
                            dialog.getButton(BUTTON_POSITIVE).setTextColor(
                                ContextCompat.getColor(
                                    applicationContext,
                                    R.color.defaultAccentColor
                                )
                            )

                        }
                    }
                }

            }
        }
    }

    private fun purchase(skuDetails: SkuDetails?){

        skuDetails?.let {
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(it)
                .build()
            billingClient?.launchBillingFlow(this, billingFlowParams)?.responseCode
        }?:Toast.makeText(this, "상품 정보를 불러올 수 없습니다.", Toast.LENGTH_LONG).show()
    }



    private fun queryOneTimeProducts(){
        val skuListToQuery = ArrayList<String>()
        skuListToQuery.add("no_ads")
        val params = SkuDetailsParams.newBuilder()
        params
            .setSkusList(skuListToQuery)
            .setType(BillingClient.SkuType.INAPP)

        billingClient!!.querySkuDetailsAsync(params.build(),
            SkuDetailsResponseListener { p0, skuDetailsList -> // 상품 정보를 가지고 오지 못한 경우
                if (p0.responseCode != BillingClient.BillingResponseCode.OK) {
                    Toast.makeText(this, "상품 정보를 불러오던 중 오류가 발생했습니다.\n" +
                                "오류코드: + ${p0.responseCode}", Toast.LENGTH_LONG).show()
                    return@SkuDetailsResponseListener
                }
                if (skuDetailsList == null) {
                    Toast.makeText(this, "상품 정보가 존재하지 않습니다.", Toast.LENGTH_LONG).show()
                    return@SkuDetailsResponseListener
                }
                //응답 받은 데이터들의 숫자를 출력
                Log.e("Tag", "응답 받은 데이터 숫자: " + skuDetailsList.size)

                //받아온 상품 정보를 차례로 호출
                for (skuDetails in skuDetailsList) {
                    Log.e("Tag", skuDetailsList.toString())
                    skuDetail = skuDetails
                }
            })

        }

    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {

        if (p0.responseCode == BillingClient.BillingResponseCode.OK && p1 != null) {
            for (purchase in p1) {
                Toast.makeText(this, "결제 성공", Toast.LENGTH_LONG).show()
                handleNonConsumablePurchase(purchase)
            }
        } else if (p0.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(this, "사용자에 의해 결제취소", Toast.LENGTH_LONG).show()
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            Toast.makeText(this, "결제가 취소 되었습니다. 종료코드: " + p0.responseCode, Toast.LENGTH_LONG).show()
            // Handle any other error codes.
        }

    }

    private fun handleNonConsumablePurchase(purchase: Purchase) {
        Log.v("TAG_INAPP", "handlePurchase : $purchase")
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken).build()
                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    val billingResponseCode = billingResult.responseCode
                    val billingDebugMessage = billingResult.debugMessage

                    Log.v("TAG_INAPP", "response code: $billingResponseCode")
                    Log.v("TAG_INAPP", "debugMessage : $billingDebugMessage")

                }
            }
        }
    }

}


