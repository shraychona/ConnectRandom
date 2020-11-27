package com.shray.connectrandom.views.fragments

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.afollestad.assent.Permission
import com.afollestad.assent.runWithPermissions
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shray.connectrandom.R
import com.shray.connectrandom.views.activities.MainActivity
import com.shray.connectrandom.views.utils.inflate
import kotlinx.android.synthetic.main.dialog_finding_partner.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*

/**
 * @author shraychona@gmail.com
 * @since 26 Nov 2020
 */
class HomeFragment : Fragment() {

    companion object {
        const val TAG = "HomeFragment"
        private const val FIRESTORE_KEY_COLLECTION_NAME = "channels"
        private const val FIRESTORE_KEY_TIME_STAMP = "timeStamp"
        private const val FIRESTORE_KEY_IS_CONSUMED = "isConsumed"
    }

    /**
     * Variables
     */
    private val db by lazy { Firebase.firestore.collection(FIRESTORE_KEY_COLLECTION_NAME) }
    private var countdownTimer: CountDownTimer? = null

    private var isRunning: Boolean = false
    private var dialog: Dialog? = null
    private var documentListener: ListenerRegistration? = null


    /**
     * Fragment lifecycle methods
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = container?.inflate(layoutRes = R.layout.fragment_home)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // AdMobs methods
        MobileAds.initialize(activity) {}
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)


        // Click Listener
        ibConnectCall.setOnClickListener {
            runWithPermissions(Permission.RECORD_AUDIO, Permission.CAMERA) {
                findChannelId()
            }
        }
    }

    /**
     * UI methods to show while user is waiting for other user
     */

    private fun startTimer() {
        isRunning = true
        countdownTimer = object : CountDownTimer(15000, 1000) {
            override fun onFinish() {
                hideDialog()
                Toast.makeText(
                    requireContext(),
                    "No user found please try again after some time",
                    Toast.LENGTH_SHORT
                ).show()
                isRunning = false
            }

            override fun onTick(p0: Long) {
            }
        }
        countdownTimer?.start()
    }

    private fun showDialog() {
        dialog = MaterialDialog(requireContext()).show {
            cancelable(false)
            cancelOnTouchOutside(false)
            cornerRadius(res = R.dimen.dialog_corner_radius)
            title(R.string.home_Scree_dialog_title)
            message(R.string.home_Scree_dialog_body)
            negativeButton(R.string.home_screen_dialog_cancel) {
                countdownTimer?.cancel()
                isRunning = false
                documentListener?.remove()
                hideDialog()
            }
        }
    }

    private fun hideDialog() {
        dialog?.dismiss()
        dialog = null
    }

    /**
     * Firestore methods to manage channel ids
     */
    private fun findChannelId() {
        showDialog()
        val calenderTime = Calendar.getInstance()
        calenderTime.add(Calendar.SECOND, -15)
        val timestamp = Timestamp(Date(calenderTime.time.time))

        db.whereGreaterThanOrEqualTo(FIRESTORE_KEY_TIME_STAMP, timestamp)
            .whereEqualTo(FIRESTORE_KEY_IS_CONSUMED, false).get()
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

        documentRef.update(FIRESTORE_KEY_IS_CONSUMED, true)
            .addOnSuccessListener {
                // connect with channel id
                Log.d(TAG, "Update success")
                Log.d(TAG, "video call started")
                startVideoCall(documentId)
            }
            .addOnFailureListener {
                Log.d(TAG, "update failed")
            }
    }

    private fun createNewChannel() {
        val newChannel = hashMapOf(
            FIRESTORE_KEY_IS_CONSUMED to false,
            FIRESTORE_KEY_TIME_STAMP to Timestamp(Date())
        )
        db.add(newChannel)
            .addOnSuccessListener {
                startTimer()
                observeCreatedChannel(it.id)
                Log.d(TAG, "Add success")
            }
            .addOnFailureListener {
                Log.d(TAG, "Add failed")
            }
    }

    private fun observeCreatedChannel(documentId: String) {
        documentListener = db.document(documentId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed.", error)
                return@addSnapshotListener
            }

            if (isRunning) {
                if (snapshot != null && snapshot.exists()) {
                    if (snapshot.data?.getValue(FIRESTORE_KEY_IS_CONSUMED) as Boolean) {
                        Log.d(TAG, "Current data: ${snapshot.data}")
                        Log.d(TAG, "video call started")
                        countdownTimer?.cancel()
                        isRunning = false
                        startVideoCall(documentId)
                    }
                } else {
                    Log.d(TAG, "Current data: null")
                }
            } else return@addSnapshotListener
        }
    }

    // start Video call
    private fun startVideoCall(channelId: String) {
        hideDialog()
        (activity as MainActivity).doFragmentTransaction(
            VideoCallFragment.newInstance(channelId),
            VideoCallFragment.TAG
        )
    }

    override fun onDestroyView() {
        countdownTimer?.cancel()
        documentListener?.remove()
        super.onDestroyView()
    }
}