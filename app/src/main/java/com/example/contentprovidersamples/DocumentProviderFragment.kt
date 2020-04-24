package com.example.contentprovidersamples

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment

class DocumentProviderFragment : Fragment() {
    private lateinit var createDocumentButton: Button
    private lateinit var deleteDocumentButton: Button
    private lateinit var openDocumentButton: Button
    private lateinit var openDirectoryButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_document_provider, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createDocumentButton = view.findViewById(R.id.button_create_document)
        deleteDocumentButton = view.findViewById(R.id.button_delete_document)
        openDocumentButton = view.findViewById(R.id.button_open_document)
        openDirectoryButton = view.findViewById(R.id.button_open_directory)

        createDocumentButton.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.add(R.id.fragment_container, CreateDocumentFragment())
                ?.addToBackStack(null)
                ?.commit()
        }
        deleteDocumentButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
            }

            startActivityForResult(intent, DELETE_DOCUMENT)
        }
        openDocumentButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
            }

            startActivityForResult(intent, OPEN_DOCUMENT)
        }
        openDirectoryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivityForResult(intent, OPEN_DIRECTORY)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            context?.contentResolver?.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            when (requestCode) {
                OPEN_DOCUMENT -> {
                    activity?.supportFragmentManager?.beginTransaction()
                        ?.add(R.id.fragment_container, DocumentPreviewFragment.newInstance(uri))
                        ?.addToBackStack(null)
                        ?.commit()
                }
                DELETE_DOCUMENT -> {
                    deleteDocument(uri)
                }
                OPEN_DIRECTORY -> {
                    Log.d(TAG, "onActivityResult uri: $uri")
                    (activity as MainActivity).showDirectoryContent(uri)
                }
            }
        }
    }

    private fun deleteDocument(uri: Uri) {
        val contentResolver = context?.contentResolver ?: return
        val builder = AlertDialog.Builder(context)
            .setTitle("删除文件")
            .setMessage("确定要删除 ${displayName(uri)} 吗？")
            .setPositiveButton("确定") { dialog, _ ->
                DocumentsContract.deleteDocument(contentResolver, uri)
                dialog?.dismiss()
                Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消") { dialog, _ -> dialog?.dismiss() }
        builder.create().show()
    }

    private fun displayName(uri: Uri): String? {
        val cursor = context?.contentResolver?.query(uri, null,
            null, null, null, null) ?: return null
        cursor.moveToFirst()
        val displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        cursor.close()
        return displayName
    }

    companion object {
        val TAG = DocumentProviderFragment::class.java.simpleName

        const val OPEN_DOCUMENT = 2
        const val DELETE_DOCUMENT = 3
        const val OPEN_DIRECTORY = 4
    }
}