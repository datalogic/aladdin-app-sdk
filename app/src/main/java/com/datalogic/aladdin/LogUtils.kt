package com.datalogic.aladdin

import android.util.Log

/**
 * This class contains Log util functions
 */
class LogUtils {
    companion object {
        private const val logEnabled = true

        /**
         * Prints debug message if log is enabled
         * @param message debug message
         * @param tag message tag
         */
        fun debug(tag: String, message: String) {
            if (logEnabled) {
                Log.d(tag, message)
            }
        }

        /**
         * Prints information if log is enabled
         * @param message info message
         * @param tag message tag
         */
        fun info(tag: String, message: String) {
            if (logEnabled) {
                Log.i(tag, message)
            }
        }

        /**
         * Prints error message if log is enabled
         * @param message error message
         * @param tag message tag
         */
        fun error(tag: String, message: String) {
            if (logEnabled) {
                Log.e(tag, message)
            }
        }

        /**
         * Prints warning message if log is enabled
         * @param message warning message
         * @param tag message tag
         */
        fun warn(tag: String, message: String) {
            if (logEnabled) {
                Log.w(tag, message)
            }
        }
    }
}