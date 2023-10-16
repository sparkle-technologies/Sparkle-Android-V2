package com.cyberflow.sparkle.chat.common.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.abedelazizshe.lightcompressorlibrary.CompressionListener
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.VideoQuality
import com.abedelazizshe.lightcompressorlibrary.config.Configuration
import com.abedelazizshe.lightcompressorlibrary.config.SaveLocation
import com.abedelazizshe.lightcompressorlibrary.config.SharedStorageConfiguration
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener
import java.io.File

class CompressFileEngineImpl {
    companion object {

        val TAG = "CompressFileEngineImpl"

         const val ORIGIN_VIDEO_MAX_SIZE = 10
         const val VIDEO_SIZE_VERY_LOW = 96
         const val VIDEO_SIZE_LOW = 49
         const val VIDEO_SIZE_MEDIUM = 33
         const val VIDEO_SIZE_HIGH = 25
         const val VIDEO_SIZE_VERY_HIGH = 26

        fun check(
            context: Context, source: ArrayList<Uri>, call: OnKeyValueResultCallbackListener
        ) {
            // size1=49.50347MB  	 size2=5.128317MB  	      percent=0.89640486   VERY_LOW
            // size1=49.50347MB  	 size2=10.031748MB  	  percent=0.7973527    Low
            // size1=49.50347MB  	 size2=14.908022MB  	  percent=0.698849     MEDIUM
            // size1=49.50347MB  	 size2=19.796103MB  	  percent=0.6001068    HIGH
            // size1=49.50347MB  	 size2=29.458006MB  	  percent=0.4049305    VERY_HIGH


            // 495/5.12=96MB   VERY_LOW
            // 495/10.03=49MB  Low
            // 495/14.9=33MB   MEDIUM
            // 495/19.79=25MB  HIGH
            // 495/29.45=16MB  VERY_HIGH
            // 10MB

            if(source.isNullOrEmpty()){
                call.onCallback("", "")
                return
            }
            val file = File(source[0].path)
            val size = file.length().toFloat() / (1024 * 1024)  // MB
            Log.e(TAG, "before compress,  video size: $size" )

            var videoQuality = VideoQuality.VERY_LOW

            if(size >= VIDEO_SIZE_LOW){
                videoQuality = VideoQuality.VERY_LOW
            }
            if( size >= VIDEO_SIZE_MEDIUM && size < VIDEO_SIZE_LOW){
                videoQuality = VideoQuality.LOW
            }
            if( size >= VIDEO_SIZE_HIGH && size < VIDEO_SIZE_MEDIUM){
                videoQuality = VideoQuality.MEDIUM
            }
            if( size >= VIDEO_SIZE_VERY_HIGH && size < VIDEO_SIZE_HIGH){
                videoQuality = VideoQuality.HIGH
            }
            if( size >= ORIGIN_VIDEO_MAX_SIZE && size < VIDEO_SIZE_VERY_HIGH){
                videoQuality = VideoQuality.VERY_HIGH
            }

            Log.e(TAG, "before compress,   videoQuality: $videoQuality" )

            VideoCompressor.start(
                context = context,
                uris = source,
                isStreamable = false,
                sharedStorageConfiguration = SharedStorageConfiguration(
                    saveAt = SaveLocation.movies, subFolderName = "sparkle-compress-videos"
                ),
                configureWith = Configuration(
                    quality = videoQuality,
                    videoNames = source.map { uri -> uri.pathSegments.last() },
                    isMinBitrateCheckEnabled = true,
                ),
                listener = object : CompressionListener {
                    override fun onProgress(index: Int, percent: Float) {
//                        Log.e(TAG, "onStartCompress: onProgress  ")
                    }

                    override fun onStart(index: Int) {

                    }

                    override fun onSuccess(index: Int, size: Long, path: String?) {
                        call?.onCallback(source[index].path, path)
                    }

                    override fun onFailure(index: Int, failureMessage: String) {
                        Log.e("failureMessage", failureMessage)
                        call.onCallback("", "")
                    }

                    override fun onCancelled(index: Int) {
                        Log.e("TAG", "compression has been cancelled")
                        // make UI changes, cleanup, etc
                    }
                },
            )
        }
    }
}

