package com.datalogic.aladdinsdk

import android.util.Log

/**
 * This class contains Log util functions
 */
internal class LogUtils {
    companion object {
        private const val logEnabled = true
        const val TAG = "AladdinSDK"

        /**
         * Prints debug message if log is enabled
         * @param message debug message
         */
        fun debug(message: String) {
            if (logEnabled) {
                Log.d(TAG, message)
            }
        }

        /**
         * Prints information if log is enabled
         * @param message info message
         */
        fun info(message: String) {
            if (logEnabled) {
                Log.i(TAG, message)
            }
        }

        /**
         * Prints error message if log is enabled
         * @param message error message
         */
        fun error(message: String) {
            if (logEnabled) {
                Log.e(TAG, message)
            }
        }

        /**
         * Prints warning message if log is enabled
         * @param message warning message
         */
        fun warn(message: String) {
            if (logEnabled) {
                Log.w(TAG, message)
            }
        }
    }
}