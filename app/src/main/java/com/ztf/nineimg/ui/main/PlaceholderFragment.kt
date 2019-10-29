package com.ztf.nineimg.ui.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.ztf.nineimg.GetPhotoFromAlbum
import com.ztf.nineimg.R
import com.ztf.nineimg.SdCardTools
import java.io.File
import java.lang.Exception

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel

    private lateinit var imageView: ImageView
    //    private lateinit var tvFilePath: TextView
    private lateinit var btn: Button
    //图片路径
    var photoPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main, container, false)
        val textView: TextView = root.findViewById(R.id.section_label)
        pageViewModel.text.observe(this, Observer<String> {
            textView.text = it
        })
        imageView = root.findViewById(R.id.image)
//        tvFilePath = root.findViewById(R.id.image_path)
        btn = root.findViewById(R.id.splite)
        imageView.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (context?.let { it1 ->
                        ContextCompat.checkSelfPermission(
                            it1,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    } != PackageManager.PERMISSION_GRANTED
                ) {
                    activity?.let { it1 ->
                        ActivityCompat.requestPermissions(
                            it1,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            9
                        )
                    }
                } else {
                    //权限已经被授权，开启相册
                    openGell()
                }
            }
        }
        btn.setOnClickListener {
            callPythonCode()
        }
        initPython()
        return root
    }

    private fun initPython() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
    }

    private fun callPythonCode() {
        val python = Python.getInstance()
        val folderPath = SdCardTools.getRootPath() + "/Pictures/"
        val folder = File(folderPath)
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val handler = Handler()
        Thread(Runnable {
            handler.post {
                btn.isClickable = false
                btn.text = "正在切..."
            }
            python.getModule("Img2nine").callAttr("img_2_nine", photoPath, folderPath)
            handler.post {
                btn.isClickable = true
                btn.text = "大卸九块"
                Toast.makeText(context, "切完了", Toast.LENGTH_LONG).show()
                refreshGell()
            }
        }).start()
    }

    private fun refreshGell() {
        // 其次把文件插入到系统图库
        val folderPath = SdCardTools.getRootPath() + "/Pictures/"
        val file = File(photoPath);
        val fileName = file.name
        val simpleName = fileName.split(".")[0]
        for (i in (1..9)) {
            val curName = simpleName + "_" + i + ".jpg"

            try {
                MediaStore.Images.Media.insertImage(
                    context!!.contentResolver,
                    folderPath + curName, curName, null
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // 最后通知图库更新
        context!!.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://$folderPath")))
    }

    private fun openGell() {
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.type = "image/*"
        startActivityForResult(intent, 9)
    }

    //拍照功能
    private fun goCamera() {

//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            uri = FileProvider.getUriForFile(context, "com.ztf.fileprovider", cameraSavePath)
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//
//        } else {
//            uri = Uri.fromFile(cameraSavePath)
//        }
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
//        this.startActivityForResult(intent, 10)
    }

    //活动请求的回调，用requestCode来匹配
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        //相册
        if (requestCode == 9 && resultCode == Activity.RESULT_OK) {

            photoPath = data!!.data?.let { context?.let { it1 -> GetPhotoFromAlbum.getRealPathFromUri(it1, it) } }!!
            imageView.setImageURI(Uri.parse(photoPath))
//            tvFilePath.text = photoPath

            //拍照
        }
//        else if (requestCode == ACTIVITY_REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
//
//            photoPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                cameraSavePath.toString()
//            } else {
//                uri.encodedPath
//            }
//            imageView.setImageURI(Uri.parse(photoPath))
//        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    //权限结果回调
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        when (requestCode) {

            //相册权限请求结果
            9 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGell()
                } else {
                    Toast.makeText(context, "你拒绝了读取相册权限", Toast.LENGTH_SHORT).show()
                }
            }

            //拍照权限请求结果
            10 -> {
                //用于判断是否有未授权权限，没有则开启照相
                var isAgree = true
                for (i in grantResults.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //检查到有未授予的权限
                        isAgree = false
                        //判断是否勾选禁止后不再询问
                        val showRequestPermission =
                            activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, permissions[i]) }
                        if (showRequestPermission!!) {
                            Toast.makeText(context, "你拒绝了拍照相关权限", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                //isAgree没有被置为false则表示权限都已授予，开启拍照
                if (isAgree) {
                    goCamera()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}