package com.itismob.grpfive.mco.utils

// Using singleton pattern
object Validator {
    // Password rules: min 8 chars, at least 1 lowercase, 1 uppercase, 1 special char
    private val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#\$%^&*])[A-Za-z\\d!@#\$%^&*]{8,}\$")

    // Validate email; returns null if valid, error message otherwise
    fun validateEmail(email: String): String? {
        return if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            "Invalid email format."
        else null
    }

    // Validate length; returns null if valid, error message otherwise
    fun validateLength(field: String, min: Int = 0, max: Int = Int.MAX_VALUE): String? {
        return when {
            field.length < min -> "Must be at least $min characters"
            field.length > max -> "Must be at most $max characters"
            else -> null
        }
    }

    // Validate that password matches confirm password
    fun validatePasswordMatch(password: String, confirmPassword: String): String? {
        return if (password != confirmPassword) "Passwords do not match"
        else null
    }

    // Validate password strength; returns null if strong, error message otherwise
    fun validatePassword(password: String): String? {
        return if (!passwordPattern.matches(password))
            "Password must be at least 8 characters, include 1 uppercase, lowercase, and special character each."
        else null
    }

    fun validateLogin(email: String, password: String): String? {
        if (email.isBlank() || password.isBlank()) {
            return "Please fill up both fields."
        }

        validateEmail(email)?.let { return it }

        return null
    }
}
