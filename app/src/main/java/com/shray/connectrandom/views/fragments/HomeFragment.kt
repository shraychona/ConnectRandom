package com.shray.connectrandom.views.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
        private const val PERMISSION_REQ_ID = 22
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = container?.inflate(layoutRes = R.layout.fragment_home)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MobileAds.initialize(activity) {}
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // If all the permissions are granted, initialize the RtcEngine object
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID) &&
            checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID)
        ) {
//            initializeAgoraVariables()
        }
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        Log.i(VideoCallFragment.TAG, "checkSelfPermission $permission $requestCode")
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(permission),
                requestCode
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}