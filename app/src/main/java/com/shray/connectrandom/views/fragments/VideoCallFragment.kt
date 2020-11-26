package com.shray.connectrandom.views.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.shray.connectrandom.R
import com.shray.connectrandom.views.utils.inflate
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import kotlinx.android.synthetic.main.fragment_video_call.*

/**
 * @author shraychona@gmail.com
 * @since 25 Nov 2020
 */
class VideoCallFragment : Fragment() {

    companion object {
        const val TAG = "VideoCallFragment"
        private const val PERMISSION_REQ_ID = 22
    }

    private var mRtcEngine: RtcEngine? = null
    private lateinit var mRtcEventHandler: IRtcEngineEventHandler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = container?.inflate(layoutRes = R.layout.fragment_video_call)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If all the permissions are granted, initialize the RtcEngine object
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID) &&
            checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID)
        ) {
            initializeAgoraVariables()
        }
    }

    private fun initializeAgoraVariables() {
        mRtcEventHandler = object : IRtcEngineEventHandler() {
            // Listen for the onFirstRemoteVideoDecoded callback.
            // This callback occurs when the first video frame of a remote user is received and decoded after the remote user successfully joins the channel.
            // You can call the setupRemoteVideo method in this callback to set up the remote video view.
            override fun onFirstRemoteVideoDecoded(
                uid: Int,
                width: Int,
                height: Int,
                elapsed: Int
            ) {
                activity?.runOnUiThread { setupRemoteVideo(uid) }
            }

            // Listen for the onUserOffline callback.
            // This callback occurs when the remote user leaves the channel or drops offline.
            override fun onUserOffline(uid: Int, reason: Int) {
                activity?.runOnUiThread { onRemoteUserLeft(uid) }
            }
        }
        try {

            mRtcEngine =
                RtcEngine.create(context, getString(R.string.agora_app_id), mRtcEventHandler)
            joinChannel()
            setupLocalVideo()
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
            throw RuntimeException(
                "NEED TO check rtc sdk init fatal error" + Log.getStackTraceString(e)
            )
        }
    }

    private fun setupLocalVideo() {

        // Enable the video module.
        mRtcEngine!!.enableVideo()

        val container = local_video_view_container as FrameLayout

        // Create a SurfaceView object.
        val surfaceView = RtcEngine.CreateRendererView(requireContext())
        surfaceView.setZOrderMediaOverlay(true)
        container.addView(surfaceView)
        // Set the local video view.
        mRtcEngine!!.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
    }

    private fun joinChannel() {

        // Join a channel with a token.
        mRtcEngine!!.joinChannel(null, "demoChannel1", "Extra Optional Data", 0)
    }


    private fun setupRemoteVideo(uid: Int) {
        val container = remote_video_view_container as FrameLayout

        if (container.childCount >= 1) {
            return
        }

        // Create a SurfaceView object.
        val surfaceView = RtcEngine.CreateRendererView(requireContext())
        container.addView(surfaceView)

        // Set the remote video view.
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
    }

    private fun onRemoteUserLeft(uid: Int) {
//        if (mRemoteVideo != null && mRemoteVideo.uid === uid) {
//            removeFromParent(mRemoteVideo)
//            // Destroys remote view
//            mRemoteVideo = null
//        }
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        Log.i(TAG, "checkSelfPermission $permission $requestCode")
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

    override fun onDestroy() {
        super.onDestroy()

        leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
    }

    private fun leaveChannel() {
        // Leave the current channel.
        mRtcEngine!!.leaveChannel()
    }
}