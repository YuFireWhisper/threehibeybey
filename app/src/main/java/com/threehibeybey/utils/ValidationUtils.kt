package com.threehibeybey.utils

import android.util.Patterns

/**
 * Utility class for validating user inputs.
 */
object ValidationUtils {

    /**
     * Validates if the provided email has a correct format.
     */
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Checks if the password meets the strength criteria.
     */
    fun isPasswordStrong(password: String): Boolean {
        return password.length >= 8 && password.any { it.isDigit() } && password.any { it.isUpperCase() }
    }
}
