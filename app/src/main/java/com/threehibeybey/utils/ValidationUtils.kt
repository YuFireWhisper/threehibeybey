package com.threehibeybey.utils

import android.util.Patterns

/**
 * Utility class for validating user inputs.
 */
object ValidationUtils {

    /**
     * Validates if the provided email has a correct format.
     *
     * @param email The email address to validate.
     * @return True if the email format is valid, false otherwise.
     */
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Checks if the password meets the strength criteria.
     *
     * @param password The password to validate.
     * @return True if the password is strong, false otherwise.
     */
    fun isPasswordStrong(password: String): Boolean {
        // Password must be at least 8 characters, contain at least one digit and one uppercase letter
        val passwordRegex = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}\$")
        return passwordRegex.matches(password)
    }
}
