package com.android.chat.utils

import android.util.Log
import android.view.View

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.Patterns
import android.util.TypedValue
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.android.chat.models.User
import com.bumptech.glide.Glide
import org.json.JSONArray
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
fun View.gone(){
    this.visibility = View.GONE
}
fun View.visible(){
    this.visibility = View.VISIBLE
}
fun View.invisible(){
    this.visibility = View.INVISIBLE
}


//price validation
fun String.isPriceValid() =
    this.matches("^[0-9]+(?:[.]\\d+)?\$".toRegex())

//duration validation
fun String.isDurationNotEmpty() =
    this != "00:00"

//duration validation
fun String.isDurationValid() =
    this.matches("^[0-9]{2}:[0-9]{2}\$".toRegex())

//username validation
fun String.isValidName() =
    !TextUtils.isEmpty(this)

//password validation
fun String.isValidPassword() =
    !TextUtils.isEmpty(this) && this.length > 7 && this.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])([a-zA-Z0-9]{8,})\$".toRegex())

//check if an email is valid or not
fun String.isValidEmail() =
    !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isPhoneValid() =  this.length > 9
//!TextUtils.isEmpty(this) &&

//change TextView Font FAMILY
fun TextView.setCustomFont(font: Int) {
    val typeface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        resources.getFont(font)
    } else {
        ResourcesCompat.getFont(context, font)
    }
    this.typeface = typeface

}


fun EditText.setCustomFont(font: Int) {
    val typeface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        resources.getFont(font)
    } else {
        ResourcesCompat.getFont(context, font)
    }
    this.typeface = typeface

}

//fun String.toSpannableTextWithFont(font: String, context: Context): SpannableString {
//    val txt = SpannableString(this)
//    txt.setCustomFont(font, context)
//    return txt
//}


//Loading Image URL into ImageView Using Glide Library
fun ImageView.loadImageWithGlide(imgUrl: String) {
    Glide.with(this).load(imgUrl)
        .placeholder(0)
        .into(this)
}

//Loading Image URL into ImageView Using Glide Library
fun ImageView.loadImageWithGlide(imgUrl: String, drawable: Int) {
    Glide.with(this).load(imgUrl)
        .placeholder(drawable)
        .error(drawable)
        .into(this)
}

//Loading Image URL into ImageView Using Glide Library
fun ImageView.loadImageWithGlide(imgSrc: Int) {
    Glide.with(this).load(imgSrc)
        .into(this)
}


fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}


fun View.setMargin(left: Float, top: Float, right: Float, bottom: Float) {
    val param = this.layoutParams as ViewGroup.MarginLayoutParams
    param.setMargins(dpToPx(left), dpToPx(top), dpToPx(right), dpToPx(bottom))

    this.layoutParams = param
}

fun View.setMargin(left: Int, top: Int, right: Int, bottom: Int) {

    val param = this.layoutParams as ViewGroup.MarginLayoutParams
    param.setMargins(left, top, right, bottom)

    this.layoutParams = param


}


fun View.dpToPx(dp: Float): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()

fun Context.pxToDp(px: Float): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, resources.displayMetrics).toInt()



fun Int.pad(): String {
    return if (this >= 10) {
        this.toString()
    } else {
        "0$this"
    }


}

fun String.capitalizeFirstCharacter(): String {
    return this[0].uppercase() + this.substring(1)
}

fun String.toTitleCase(): String {
    val arr = this.split(" ").toTypedArray()
    val sb = StringBuffer()
    for (i in arr.indices) {
        sb.append(Character.toUpperCase(arr[i][0]))
            .append(arr[i].substring(1)).append(" ")
    }
    return sb.toString().trim { it <= ' ' }
}

fun View.animateRotation(rotationAngle: Int) {

    this.animate().rotation(rotationAngle.toFloat()).setDuration(400).start()

}

fun Double.round(decimals: Int): Double {
    return BigDecimal(this).setScale(decimals, RoundingMode.HALF_EVEN).toDouble()
}

fun String.toValidDouble(): Double {
    return try {
        this.toDouble()
    } catch (e: NumberFormatException) {
        0.0
    }
}

@SuppressLint("SimpleDateFormat")
fun String.isValidHourMinute() : Boolean{
    return try {
        val sdf = SimpleDateFormat("HH:mm")
        val datePause1 = sdf.parse(this)
        true
    }catch (e:Exception){
        false
    }
}

fun String.toFormattedPrice(): String {
    return if (this.contains(","))
        this.replace(",", ".")
    else this

}
