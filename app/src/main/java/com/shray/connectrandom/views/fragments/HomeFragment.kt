package com.shray.connectrandom.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.afollestad.assent.Permission
import com.afollestad.assent.runWithPermissions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shray.connectrandom.R
import com.shray.connectrandom.views.utils.inflate
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*

/**
 * @author shraychona@gmail.com
 * @since 26 Nov 2020
 */
class HomeFragment : Fragment() {

    companion object {
        const val TAG = "HomeFragment"
    }

    // Variables
    private val db by lazy { Firebase.firestore.collection("channels") }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = container?.inflate(layoutRes = R.layout.fragment_home)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MobileAds.initialize(activity) {}
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        ibConnectCall.setOnClickListener {
            runWithPermissions(Permission.RECORD_AUDIO, Permission.CAMERA) {
                findChannelId()
            }
        }

    }

    private fun findChannelId() {
        val calenderTime = Calendar.getInstance()
        calenderTime.add(Calendar.SECOND, -15)
        val timestamp = Timestamp(Date(calenderTime.time.time))

        db.whereGreaterThanOrEqualTo(getString(R.string.firestore_key_time_stamp), timestamp)
            .whereEqualTo(getString(R.string.firestore_key_is_consumed), false).get()
            .addOnSuccessListener {
                if (it.documents.isNotEmpty()) {
                    val document = it.documents.first()
                    consumeChannel(document.id)
                } else createNewChannel()
            }.addOnFailureListener {
                Log.d(TAG, "Fetch failed : ${it.message}")
            }
    }

    private fun consumeChannel(documentId: String) {
        val documentRef = db.document(documentId)

        documentRef.update(getString(R.string.firestore_key_is_consumed), true)
            .addOnSuccessListener {
                // connect with channel id
                Log.d(TAG, "Update success")
            }
            .addOnFailureListener {
                Log.d(TAG, "update failed")
            }
    }

    private fun createNewChannel() {
        val newChannel = hashMapOf(
            getString(R.string.firestore_key_is_consumed) to false,
            getString(R.string.firestore_key_time_stamp) to Timestamp(Date())
        )
        db.add(newChannel)
            .addOnSuccessListener {
                Log.d(TAG, "Add success")
            }
            .addOnFailureListener {
                Log.d(TAG, "Add failed")
            }
    }
}