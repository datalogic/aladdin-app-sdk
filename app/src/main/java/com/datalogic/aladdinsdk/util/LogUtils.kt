package com.datalogic.aladdinsdk.util

import android.util.Log
import com.datalogic.aladdinsdk.BuildConfig

class LogUtils {
    companion object {
        private const val LOG_TAG = "BLE_SDK"
        private val logEnabled = BuildConfig.DEBUG

        /**
         * Prints debug message if log is enabled
         * @param message debug message
         */
        fun debug(message: String) {
            if (logEnabled) {
                Log.d(LOG_TAG, message)
            }
        }

        /**
         * Prints information if log is enabled
         * @param message info message
         */
        fun info(message: String) {
            if (logEnabled) {
                Log.i(LOG_TAG, message)
            }
        }

        /**
         * Prints error message if log is enabled
         * @param message error message
         */
        fun error(message: String) {
            if (logEnabled) {
                Log.e(LOG_TAG, message)
            }
        }

        /**
         * Prints warning message if log is enabled
         * @param message warning message
         */
        fun warn(message: String) {
            if (logEnabled) {
                Log.w(LOG_TAG, message)
            }
        }
    }
}