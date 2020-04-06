package com.racoondog.gotaplan

import android.Manifest
import android.app.AlertDialog
import android.app.AlertDialog.Builder
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.schedule_dialog.*
import kotlinx.android.synthetic.main.weekview.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.system.exitProcess


class ScheduleDialog:Dialog {

    private val realm = Realm.getDefaultInstance()
    lateinit var cnxt:MainActivity

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
        setContentView(R.layout.schedule_dialog)

        editScheduleTitle.setOnClickListener {
            val dialog = EditScheduleTitleDialog(context)
            dialog.cnxt = this
            dialog.show()
            dismiss()
        }

        editScheduleTime.setOnClickListener {
            val dialog = ScheduleTimeDialog(context)
            dialog.cnxt = this
            dialog.show()
            dismiss()
        }

        saveSchedule.setOnClickListener{

            cnxt.checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE){
                val bitmap1 = getBitmapFromView(cnxt.scheduleView, cnxt.scheduleView.height, cnxt.scheduleView.width)
                val bitmap2 = getBitmapFromView(cnxt.dayLine, cnxt.dayLine.height, cnxt.dayLine.width)
                val bitmap3 = getBitmapFromView(cnxt.main_toolbar, cnxt.main_toolbar.height, cnxt.main_toolbar.width)
                val bitmap = combineImages(bitmap1, bitmap2, bitmap3)
                saveBitmap(bitmap)
                dismiss()}

        }

        shareSchedule.setOnClickListener {

            cnxt.checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE){

                val bitmap1 = getBitmapFromView(cnxt.scheduleView, cnxt.scheduleView.height, cnxt.scheduleView.width)
                val bitmap2 = getBitmapFromView(cnxt.dayLine, cnxt.dayLine.height, cnxt.dayLine.width)
                val bitmap3 = getBitmapFromView(cnxt.main_toolbar, cnxt.main_toolbar.height, cnxt.main_toolbar.width)
                val bitmap = combineImages(bitmap1, bitmap2, bitmap3)

                try
                {
                    val cachePath = File(context.cacheDir, "images")
                    cachePath.mkdirs() // don't forget to make the directory
                    val stream = FileOutputStream("$cachePath/image.png") // overwrites this image every time
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.close()
                }
                catch (e: IOException) {
                    e.printStackTrace()
                }

                val imagePath = File(context.cacheDir, "images")
                val newFile = File(imagePath, "image.png")
                val contentUri =
                    FileProvider.getUriForFile(context, "com.racoondog.gotaplan.fileprovider", newFile)

                if (contentUri != null) {
                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
                    shareIntent.setDataAndType(contentUri, cnxt.contentResolver.getType(contentUri))
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
                    cnxt.startActivity(Intent.createChooser(shareIntent,
                        cnxt.applicationContext.resources.getString(R.string.schedule_dialog_share_schedule)))
                }

                dismiss()
            }

        }


    }

    private fun getBitmapFromView(view: View, height:Int, width:Int): Bitmap {

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun combineImages(first: Bitmap, second: Bitmap, third: Bitmap): Bitmap {

        val bitmap = Bitmap.createBitmap(first.width, first.height+second.height+third.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(third, Matrix(), null)
        canvas.drawBitmap(second,0.toFloat(), third.height.toFloat(), null)
        canvas.drawBitmap(first, 0.toFloat(), third.height.toFloat()+second.height.toFloat(), null)

        return bitmap
    }

    private fun saveBitmap(bitmap:Bitmap) { // 버튼 onClick 리스너
        // WRITE_EXTERNAL_STORAGE 외부 공간 사용 권한 허용

        val fos: FileOutputStream // FileOutputStream 이용 파일 쓰기 한다
        val strFolderPath = Environment.getExternalStorageDirectory().absolutePath + "/Public"
        val folder = File(strFolderPath)
        if (!folder.exists())
        { // 해당 폴더 없으면 만들어라
            folder.mkdirs()
        }
        val strFilePath = strFolderPath + "/" + System.currentTimeMillis() + ".png"
        val fileCacheItem = File(strFilePath)
        try
        {
            fos = FileOutputStream(fileCacheItem)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
        }
        catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        finally
        {
            Toast.makeText(context,cnxt.applicationContext.resources.getString(R.string.save_timetable_gallery) , Toast.LENGTH_SHORT).show()
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(strFilePath))))
        }

    }

}