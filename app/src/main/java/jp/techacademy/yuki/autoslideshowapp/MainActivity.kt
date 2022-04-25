package jp.techacademy.yuki.AutoSlideshowApp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import android.provider.MediaStore
import android.content.ContentUris
import android.net.Uri
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.os.Handler

class MainActivity : AppCompatActivity() { //classの宣言: 継承元

    private val PERMISSIONS_REQUEST_CODE = 100//なんぼでもいい
    var urilist = arrayListOf<Uri>() // val 配列名 = arrayOf<型>(配列の要素）
    var index = 0
    var slideclick = 1
    private var mTimer: Timer? = null
    private var mHandler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                } else {
                    Toast.makeText(applicationContext, "権限がありません", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun getContentsInfo() { // 画像の情報を取得するメソッド
        val resolver = contentResolver//ContentResolverはContentProviderのデータを参照するためのクラス（・・・？）
        val cursor = resolver.query(//ContentResolverクラスのqueryメソッドを使って条件を指定して検索して情報を取得
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類（これは外部ストレージ）
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {//cursor!!.moveToFirst() は検索結果の最初のデータをさす
            // indexからIDを取得し、そのIDから画像のURIを取得する
            do {//do以下を処理する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                //cursor.getColumnIndex()で現在cursorが指しているデータの中から画像のIDがセットされている位置を取得
                val id = cursor.getLong(fieldIndex)
                //cursor.getLong()で画像のID
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                //ContentUris.withAppendedId()でそこから実際の画像のURIを取得

                urilist.add(imageUri)//urilistにimageuriを追加

            } while (cursor.moveToNext())//falseを返すまで繰り返す

            imageView.setImageURI(urilist.get(index))//index番目のurilistの画像を表示

        }
        cursor.close()

        foward_button.setOnClickListener {//foward_button にクリックを検知させる
            index++ //foward_button が押されたら indexを1ずつ＋

            if (index == urilist.size)//もし indexがurilistの要素の数と同じ数の時
            {
                index = 0
            } //indexに0を代入する

            imageView.setImageURI(urilist.get(index)) //index番目の情報を受け取ったUrilist
            // imageViewに取得したURIのイメージをセットする
        }

        back_button.setOnClickListener {//back_button にクリックを検知させる

            if (index > 0) {
                index--
                //indexが0より大きいときindexを1ずつ-する
            } else if (index == 0) {
                index = urilist.size - 1
            }

            imageView.setImageURI(urilist.get(index)) //index番目の情報を受け取ったUrilist
            // imageViewに取得したURIのイメージをセットする


        }
        slideshow_button.setOnClickListener {
            foward_button.setEnabled(false)
            back_button.setEnabled(false)
            slideshow_button.text = "停止"
            slideclick ++

            if (mTimer == null && slideclick%2 ==0) {
                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        mHandler.post {
                            if (index == urilist.size - 1) {
                                index = 0
                            } else {
                                index++
                            }

                            imageView.setImageURI(urilist.get(index))

                        }
                    }
                }, 2000, 2000) // 最初に始動させるまで100ミリ秒、ループの間隔を100ミリ秒 に設定
            }

            else {
                foward_button.setEnabled(true)
                back_button.setEnabled(true)
                slideshow_button.text = "再生"
                if (mTimer != null){
                    mTimer!!.cancel()
                    mTimer = null
                }
            }
        }

    }

}

