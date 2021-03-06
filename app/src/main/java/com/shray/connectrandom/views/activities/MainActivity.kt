package com.shray.connectrandom.views.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shray.connectrandom.R
import com.shray.connectrandom.views.fragments.HomeFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.flFragContainer, HomeFragment(), HomeFragment.TAG)
        fragmentTransaction.commit()
    }
}