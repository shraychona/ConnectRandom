package com.shray.connectrandom.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
        private const val BUNDLE_EXTRAS_CHANNEL_ID = "channelId"

        fun newInstance(channelId: String): VideoCallFragment {
            return VideoCallFragment().apply {
                arguments = Bundle().apply {
                    putString(BUNDLE_EXTRAS_CHANNEL_ID, channelId)
                }
            }
        }
    }

    private var mRtcEngine: RtcEngine? = null
    private lateinit var mRtcEventHandler: IRtcEngineEventHandler
    private val channelId by lazy { requireArguments().getString(BUNDLE_EXTRAS_CHANNEL_ID, "") }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = container?.inflate(layoutRes = R.layout.fragment_video_call)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeAgoraVariables()
        setupLocalVideo()
        joinChannel()

        ivEndCall.setOnClickListener {
            leaveChannel()
            activity?.supportFragmentManager?.popBackStackImmediate()
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
                activity?.runOnUiThread { onRemoteUserLeft() }
            }
        }
        try {
            mRtcEngine =
                RtcEngine.create(context, getString(R.string.agora_app_id), mRtcEventHandler)
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
        mRtcEngine!!.muteLocalAudioStream(false)
    }

    private fun joinChannel() {

        // Join a channel with a token.
        mRtcEngine!!.joinChannel(null, channelId, "Random Video", 0)
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

    private fun onRemoteUserLeft() {
        leaveChannel()
        activity?.supportFragmentManager?.popBackStackImmediate()
    }

    override fun onDestroy() {
        super.onDestroy()

        mRtcEngine!!.leaveChannel()
        mRtcEngine = null
        RtcEngine.destroy()
    }

    private fun leaveChannel() {
        // Leave the current channel.
        val remoteContainer = remote_video_view_container as FrameLayout
        remoteContainer.removeAllViews()
        val selfContainer = local_video_view_container as FrameLayout
        selfContainer.removeAllViews()
        mRtcEngine!!.leaveChannel()
    }
}