package com.example.contentprovidersamples

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, MainFragment())
                .commit()
        }
    }

    fun showDirectoryContent(directoryUri: Uri) {
        Log.d(TAG, "directory uri: $directoryUri")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DirectoryListFragment.newInstance(directoryUri), directoryUri.toString())
            .addToBackStack(directoryUri.toString())
            .commit()
    }

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }
}
