package com.example.contentprovidersamples

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder

class DocumentPreviewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_document_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contentTextView = view.findViewById<TextView>(R.id.text_view_content)

        val documentUri = Uri.parse(arguments?.getString(KEY_DOCUMENT_URI)) ?: return
        val contentResolver = context?.contentResolver ?: return
        val content = StringBuilder()
        Thread {
            contentResolver.openInputStream(documentUri).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream ?: return@use)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        content.append(line)
                        line = reader.readLine()
                    }
                }
            }
            activity?.runOnUiThread {
                contentTextView.text = content
            }
        }.start()
    }

    companion object {
        const val KEY_DOCUMENT_URI = "document_uri"

        fun newInstance(documentUri: Uri): DocumentPreviewFragment {
            return DocumentPreviewFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_DOCUMENT_URI, documentUri.toString())
                }
            }
        }
    }
}