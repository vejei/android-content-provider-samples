package com.example.contentprovidersamples

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.ContentValues
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MediaStoreFragment : Fragment() {
    private lateinit var listImageButton: Button
    private lateinit var takeScreenshotButton: Button
    private lateinit var deleteLastImageButton: Button
    private lateinit var imageList: RecyclerView
    private lateinit var imageAdapter: ImageAdapter
    private val images = mutableListOf<Image>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_media_store, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listImageButton = view.findViewById(R.id.button_media_store_list_image)
        takeScreenshotButton = view.findViewById(R.id.button_media_store_take_screenshot)
        deleteLastImageButton = view.findViewById(R.id.button_media_store_delete_last_image)
        imageList = view.findViewById(R.id.image_list)

        listImageButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(context, "需要获取权限，请请允许权限请求", Toast.LENGTH_SHORT).show()
                }
                ActivityCompat.requestPermissions(
                    activity as Activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    LIST_IMAGE_REQUEST_CODE
                )
            } else {
                listImage()
            }
        }
        takeScreenshotButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(context, "需要获取权限，请允许权限请求", Toast.LENGTH_SHORT).show()
                }
                ActivityCompat.requestPermissions(
                    activity as Activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_EXTERNAL_REQUEST_CODE
                )
            } else {
                takeScreenshot()
            }
        }
        deleteLastImageButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Toast.makeText(context, "需要获取权限，请请允许权限请求", Toast.LENGTH_SHORT).show()
                }
                ActivityCompat.requestPermissions(
                    activity as Activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    DELETE_REQUEST_CODE
                )
            } else {
                deleteLastImage()
            }
        }

        imageAdapter = ImageAdapter()
        imageList.layoutManager = LinearLayoutManager(context)
        imageList.adapter = imageAdapter
    }

    private fun listImage() {
        images.clear()
        Log.d(TAG, "listImage()")
        Thread {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED
            )

            val cursor = context?.contentResolver?.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
            )
            cursor.use {
                if (it == null) return@use

                val idIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val dateIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

                while (it.moveToNext()) {
                    val id = it.getLong(idIndex)
                    val displayName = it.getString(nameIndex)
                    val size = it.getInt(sizeIndex)
                    val dateAdded = it.getLong(dateIndex)
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                    )
                    images.add(Image(id, displayName, size, dateAdded, uri.toString()))
                    Log.d(TAG, "id: $id, display name: $displayName, size: $size, " +
                            "date added: $dateAdded, uri: $uri")
                }
                it.close()
            }

            activity?.runOnUiThread {
                imageAdapter.data = images
            }
        }.start()
    }

    private fun takeScreenshot() {
        val view = activity?.window?.decorView ?: return
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        val contentResolver = context?.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
        }
        if (contentResolver == null) return
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return
        contentResolver.openOutputStream(uri, "w").use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            it?.close()
        }
        Toast.makeText(context, "截图已保存", Toast.LENGTH_SHORT).show()
    }

    private fun deleteLastImage() {
        val builder = AlertDialog.Builder(context)
            .setTitle("删除最后一张图片")
            .setMessage("确定要删除吗？")
            .setPositiveButton("确定", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    val contentResolver = context?.contentResolver ?: return

                    contentResolver.delete(Uri.parse(imageAdapter.deleteLast()),
                        null, null
                    )
                    Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
                    dialog?.dismiss()
                }
            })
            .setNegativeButton("取消") { dialog, _ -> dialog?.dismiss() }
        builder.create().show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty()) {
            when (requestCode) {
                LIST_IMAGE_REQUEST_CODE -> listImage()
                WRITE_EXTERNAL_REQUEST_CODE -> takeScreenshot()
                DELETE_REQUEST_CODE -> deleteLastImage()
            }
        } else {
            Toast.makeText(context, "权限未允许", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        val TAG = MediaStoreFragment::class.java.simpleName
        const val LIST_IMAGE_REQUEST_CODE = 1
        const val DELETE_REQUEST_CODE = 2
        const val WRITE_EXTERNAL_REQUEST_CODE = 3
    }
}

data class Image(
    val id: Long,
    val displayName: String,
    val size: Int,
    val dateAdded: Long,
    val uri: String
)

class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
    var data = mutableListOf<Image>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var previewImage = itemView.findViewById<ImageView>(R.id.image_view_preview)
        private var displayNameText = itemView.findViewById<TextView>(R.id.text_view_display_name)
        private var sizeText = itemView.findViewById<TextView>(R.id.text_view_size)
        private var dateAddedText = itemView.findViewById<TextView>(R.id.text_view_date_added)
        private var uriText = itemView.findViewById<TextView>(R.id.text_view_uri)

        fun bind(image: Image) {
            Glide.with(itemView).load(image.uri).into(previewImage)
            displayNameText.text = "display name: ${image.displayName}"
            sizeText.text = "size: ${image.size}"
            dateAddedText.text = "date added: ${image.dateAdded}"
            uriText.text = "uri: ${image.uri}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    fun deleteLast(): String {
        val image = data.removeAt(itemCount - 1)
        notifyDataSetChanged()
        return image.uri
    }
}