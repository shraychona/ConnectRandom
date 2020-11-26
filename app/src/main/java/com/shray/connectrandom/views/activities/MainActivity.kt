package com.shray.connectrandom.views.activities

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import com.shray.connectrandom.R
import com.shray.connectrandom.views.fragments.HomeFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        doFragmentTransaction(R.id.flFragContainer, HomeFragment(), HomeFragment.TAG)
    }

    fun doFragmentTransaction(
        @IdRes containerViewId: Int,
        fragment: androidx.fragment.app.Fragment,
        tag: String = ""
    ) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(containerViewId, fragment, tag)
        fragmentTransaction.commit()
    }
}