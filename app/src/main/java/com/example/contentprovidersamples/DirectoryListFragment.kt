package com.example.contentprovidersamples

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DirectoryListFragment : Fragment() {
    private lateinit var directoryUri: Uri
    private lateinit var adapter: DocumentEntryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_directory_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DocumentEntryAdapter(object : DocumentEntryAdapter.OnItemClickListener {
            override fun onItemClick(documentEntry: DocumentEntry) {
                if (documentEntry.isDirectory) {
                    Log.d(TAG, "is directory")
                    (activity as MainActivity).showDirectoryContent(documentEntry.uri)
                } else {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW).apply {
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            data = documentEntry.uri
                        })
                    } catch (exception: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            "No activity found to open ${documentEntry.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.adapter = adapter

        directoryUri = Uri.parse(arguments?.getString(KEY_DIRECTORY_URI)) ?: return
        Log.d(TAG, "directory uri: $directoryUri")
        loadDirectory(directoryUri)
    }

    private fun loadDirectory(uri: Uri) {
        Log.d(TAG, "loadDirectory uri: $uri")
        val documentFiles = DocumentFile
            .fromTreeUri(context?.applicationContext!!, uri)?.listFiles() ?: return
        Log.d(TAG, "loadDirectory document files size: ${documentFiles.size}")
        val documentEntries = mutableListOf<DocumentEntry>()
        documentFiles.forEach {
            documentEntries.add(DocumentEntry(it))
        }

        adapter.setEntries(documentEntries)
    }

    companion object {
        const val KEY_DIRECTORY_URI = "directory_uri"
        val TAG = DirectoryListFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(directoryUri: Uri): DirectoryListFragment {
            return DirectoryListFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_DIRECTORY_URI, directoryUri.toString())
                }
            }
        }
    }
}

data class DocumentEntry(private val documentFile: DocumentFile) {
    val name: String? by lazy { documentFile.name }
    val isDirectory: Boolean by lazy { documentFile.isDirectory }
    val uri get() = documentFile.uri
}

class DocumentEntryAdapter(
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<DocumentEntryAdapter.ViewHolder>() {
    private var entries = mutableListOf<DocumentEntry>()

    fun setEntries(entries: MutableList<DocumentEntry>) {
        this.entries.clear()
        this.entries.addAll(entries)
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val iconImage = itemView.findViewById<ImageView>(R.id.image_view_icon)
        private val fileNameText = itemView.findViewById<TextView>(R.id.text_view_file_name)

        fun bind(entry: DocumentEntry) {
            val imageRes = if (entry.isDirectory) {
                R.drawable.ic_folder
            } else {
                R.drawable.ic_file
            }
            iconImage.setImageResource(imageRes)
            fileNameText.text = entry.name

            itemView.setOnClickListener { onItemClickListener.onItemClick(entry) }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(documentEntry: DocumentEntry)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_document, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(entries[position])
    }


}