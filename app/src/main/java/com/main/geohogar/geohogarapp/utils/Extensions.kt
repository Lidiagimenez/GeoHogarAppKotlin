package com.main.geohogar.geohogarapp.utils


import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

/**
 * Toast Extensions
 */
fun AppCompatActivity.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Activity.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Snackbar Extensions
 */
fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

fun View.showSnackbarWithAction(
    message: String,
    actionText: String,
    action: (View) -> Unit
) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG)
        .setAction(actionText, action)
        .show()
}

/**
 * View Visibility Extensions
 */
fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.isVisible(): Boolean = visibility == View.VISIBLE

fun View.isGone(): Boolean = visibility == View.GONE

fun View.isInvisible(): Boolean = visibility == View.INVISIBLE

/**
 * View Enable/Disable Extensions
 */
fun View.enable() {
    isEnabled = true
    alpha = 1.0f
}

fun View.disable() {
    isEnabled = false
    alpha = 0.5f
}

/**
 * Keyboard Extensions
 */
fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    currentFocus?.let {
        imm.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * String Extensions
 */
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.capitalizeFirst(): String {
    return this.replaceFirstChar { it.uppercase() }
}

/**
 * Number Formatting Extensions
 */
fun Int.toCurrency(): String {
    val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "AR"))
    formatter.maximumFractionDigits = 0
    return formatter.format(this)
}

fun Double.toCurrency(): String {
    val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "AR"))
    formatter.maximumFractionDigits = 2
    return formatter.format(this)
}