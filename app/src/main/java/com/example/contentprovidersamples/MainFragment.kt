package com.example.contentprovidersamples

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mediaStoreButton = view.findViewById<Button>(R.id.button_main_media_store)
        val documentProviderButton = view.findViewById<Button>(R.id.button_main_document_provider)

        mediaStoreButton.setOnClickListener {
            activity?.supportFragmentManager
                ?.beginTransaction()
                ?.add(R.id.fragment_container, MediaStoreFragment())
                ?.addToBackStack(null)
                ?.commit()
        }

        documentProviderButton.setOnClickListener {
            activity?.supportFragmentManager
                ?.beginTransaction()
                ?.add(R.id.fragment_container, DocumentProviderFragment())
                ?.addToBackStack(null)
                ?.commit()
        }
    }
}