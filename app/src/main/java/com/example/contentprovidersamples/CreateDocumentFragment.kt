package com.example.contentprovidersamples

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.io.FileOutputStream

class CreateDocumentFragment : Fragment() {
    private lateinit var contentEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_document, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val saveButton = view.findViewById<Button>(R.id.button_create_document_save)
        contentEditText = view.findViewById(R.id.edit_text_create_document_content)

        saveButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
                putExtra(Intent.EXTRA_TITLE, "${System.currentTimeMillis()}.txt")
            }
            startActivityForResult(intent, CREATE_DOCUMENT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            if (requestCode == CREATE_DOCUMENT) {
                val contentResolver = context?.contentResolver ?: return
                contentResolver.openFileDescriptor(uri, "w").use {
                    FileOutputStream(it?.fileDescriptor ?: return@use).use { fos ->
                        fos.write(contentEditText.text.toString().toByteArray())
                        fos.close()
                    }
                }
                activity?.supportFragmentManager?.popBackStack()
                Toast.makeText(context, "已保存", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val CREATE_DOCUMENT = 1
    }
}