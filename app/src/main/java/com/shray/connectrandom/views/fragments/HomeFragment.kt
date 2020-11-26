package com.shray.connectrandom.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.afollestad.assent.Permission
import com.afollestad.assent.runWithPermissions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.shray.connectrandom.R
import com.shray.connectrandom.views.utils.inflate
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * @author shraychona@gmail.com
 * @since 26 Nov 2020
 */
class HomeFragment : Fragment() {

    companion object {
        const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = container?.inflate(layoutRes = R.layout.fragment_home)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MobileAds.initialize(activity) {}
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        runWithPermissions(Permission.RECORD_AUDIO, Permission.CAMERA) {

        }
    }
}