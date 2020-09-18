package com.vn.custom.util

import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityOptions
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vn.eoffice.R
import com.vn.eoffice.base.Base
import com.vn.eoffice.customview.DatePickerWithCancel
import com.vn.eoffice.enumApp.LanguageCodeEnum
import com.vn.eoffice.extension.toObject
import com.vn.eoffice.extension.toShow
import com.vn.eoffice.model.account.AccountDTO
import com.vn.eoffice.model.languages.LanguagesDTO
import com.vn.eoffice.model.login.Site
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.experimental.and

object GeneralUtil {

    fun getStatusBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    fun isLogined(context: Context): Boolean {
        if (PreferenceUtils.getBoolean(context, PreferenceUtils.KEY_IS_LOGOUT, true)) {
            return true
        }
        return false
    }

    fun getScreenWidth(activity: Activity): Int {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun getScreenHeight(activity: Activity): Int {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    fun initAlertDialog(context: Context, v: View, isCancelable: Boolean): AlertDialog {
        val alert = AlertDialog.Builder(context)
        alert.setView(v)
        alert.setCancelable(isCancelable)
        val alertDialog = alert.create()
        val window = alertDialog.window
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return alertDialog
    }

    fun checkIsTablet(activity: Activity): Boolean {
        val display = activity.windowManager.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)

        val widthInches = metrics.widthPixels / metrics.xdpi
        val heightInches = metrics.heightPixels / metrics.ydpi
        val diagonalInches = Math.sqrt(
            Math.pow(widthInches.toDouble(), 2.0) + Math.pow(
                heightInches.toDouble(),
                2.0
            )
        )
        return diagonalInches >= 7.0
    }

    /**
     * Check if any network is active
     *
     * @param context Context
     * @return boolean
     */


    fun showCustomToast(context: Context, msg: String, bgColor: Int) {
        val toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
        val view = toast.view

        //To change the Background of Toast
        view.setBackgroundColor(bgColor)
        val text = view.findViewById<TextView>(android.R.id.message)
        text.setTextColor(Color.WHITE)

        toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
        toast.show()
    }

    fun hideKeyboard(context: Context, v: View?) {
        if (v == null) {
            return
        }
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

    fun animateArrowExpand(imageView: ImageView) {
        animateArrowExpand(imageView, 300, null)
    }

    fun animateArrowExpand(imageView: ImageView, duration: Long, finished: (() -> Unit)?) {
        imageView.animate()
            .setDuration(duration)
            .rotation(180f)
            .start()

    }

    fun animateArrowCollapse(imageView: ImageView) {
        animateArrowCollapse(imageView, 300, null)
    }

    fun animateArrowCollapse(imageView: ImageView, duration: Long, finished: (() -> Unit)?) {
        imageView.animate()
            .setDuration(duration)
            .rotation(0f)
            .start()

    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else {
            capitalize(manufacturer) + " " + model
        }
    }

    private fun capitalize(s: String?): String {
        if (s == null || s.length == 0) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first) + s.substring(1)
        }
    }

    fun getDeviceId(context: Context): String? {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    @Throws(NoSuchAlgorithmException::class, UnsupportedEncodingException::class)
    fun makeSHA1Hash(input: String?): String? {
        if (TextUtils.isEmpty(input)) {
            return input
        }
        val md = MessageDigest.getInstance("SHA1")
        md.reset()
        val buffer = input?.toByteArray(charset("UTF-8"))
        md.update(buffer)
        val digest = md.digest()

        var hexStr = ""
        for (i in digest.indices) {
            hexStr += Integer.toString((digest[i] and 0xff.toByte()) + 0x100, 16).substring(1)
        }
        return hexStr
    }

    fun makeMD5Hash(input: String?): String? {
        if (TextUtils.isEmpty(input)) {
            return input
        }
        try {
            // Create MD5 Hash
            val digest = java.security.MessageDigest.getInstance("MD5")
            digest.update(input?.toByteArray())
            val messageDigest = digest.digest()

            // Create Hex String
            val hexString = StringBuffer()
            for (i in messageDigest.indices) {
                var h = Integer.toHexString(0xFF and messageDigest[i].toInt())
                while (h.length < 2)
                    h = "0$h"
                hexString.append(h)
            }
            return hexString.toString()

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return Constant.EMPTY_STRING
    }

    fun validEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


    /**
     * Convert a translucent themed Activity
     * [android.R.attr.windowIsTranslucent] to a fullscreen opaque
     * Activity.
     *
     *
     * Call this whenever the background of a translucent Activity has changed
     * to become opaque. Doing so will allow the [android.view.Surface] of
     * the Activity behind to be released.
     *
     *
     * This call has no effect on non-translucent activities or on activities
     * with the [android.R.attr.windowIsFloating] attribute.
     */
    fun convertActivityFromTranslucent(activity: Activity) {
        try {
            val method = Activity::class.java.getDeclaredMethod("convertFromTranslucent")
            method.isAccessible = true
            method.invoke(activity)
        } catch (t: Throwable) {
        }

    }

    /**
     * Convert a translucent themed Activity
     * [android.R.attr.windowIsTranslucent] back from opaque to
     * translucent following a call to
     * [.convertActivityFromTranslucent] .
     *
     *
     * Calling this allows the Activity behind this one to be seen again. Once
     * all such Activities have been redrawn
     *
     *
     * This call has no effect on non-translucent activities or on activities
     * with the [android.R.attr.windowIsFloating] attribute.
     */
    fun convertActivityToTranslucent(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            convertActivityToTranslucentAfterL(activity)
        } else {
            convertActivityToTranslucentBeforeL(activity)
        }
    }

    /**
     * Calling the convertToTranslucent method on platforms before Android 5.0
     */
    fun convertActivityToTranslucentBeforeL(activity: Activity) {
        try {
            val classes = Activity::class.java.declaredClasses
            var translucentConversionListenerClazz: Class<*>? = null
            for (clazz in classes) {
                if (clazz.simpleName.contains("TranslucentConversionListener")) {
                    translucentConversionListenerClazz = clazz
                }
            }
            val method = Activity::class.java.getDeclaredMethod(
                "convertToTranslucent",
                translucentConversionListenerClazz
            )
            method.isAccessible = true
            method.invoke(activity, arrayOf<Any>())
        } catch (t: Throwable) {
        }

    }

    /**
     * Calling the convertToTranslucent method on platforms after Android 5.0
     */
    private fun convertActivityToTranslucentAfterL(activity: Activity) {
        try {
            val getActivityOptions = Activity::class.java.getDeclaredMethod("getActivityOptions")
            getActivityOptions.isAccessible = true
            val options = getActivityOptions.invoke(activity)

            val classes = Activity::class.java.declaredClasses
            var translucentConversionListenerClazz: Class<*>? = null
            for (clazz in classes) {
                if (clazz.simpleName.contains("TranslucentConversionListener")) {
                    translucentConversionListenerClazz = clazz
                }
            }
            val convertToTranslucent = Activity::class.java.getDeclaredMethod(
                "convertToTranslucent",
                translucentConversionListenerClazz, ActivityOptions::class.java
            )
            convertToTranslucent.isAccessible = true
            convertToTranslucent.invoke(activity, null, options)
        } catch (t: Throwable) {
        }

    }

    fun isPhoneNumberFormat(edt: EditText): Boolean {
        var number = edt.text.toString()
        if (number.length != 10) {
            return false
        } else {
            if (number.get(0).toString() != "0") {
                return false
            }
        }
        return true
    }

    fun isMailAddress(email: String): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    fun isColor(color: String?): Boolean {
        var colorPattern = Pattern.compile("#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})")
        var m = colorPattern.matcher(color)
        var isColor = m.matches()
        return isColor
    }


    fun setTranslucentStatusBar(window: Window) {
        if (window == null) return
        var sdkInt = Build.VERSION.SDK_INT
        if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {
            setTranslucentStatusBarLollipop(window)
        } else if (sdkInt >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatusBarKiKat(window)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setTranslucentStatusBarLollipop(window: Window) {
        window.statusBarColor = window.context
            .resources
            .getColor(R.color.colorAccent)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun setTranslucentStatusBarKiKat(window: Window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }


    fun getCurrentLanguage(context: Context): LanguagesDTO {
        return (PreferenceUtils.getString(
            context,
            PreferenceUtils.KEY_LANGUAGE,
            Constant.EMPTY_STRING
        ))?.toObject<LanguagesDTO>() ?: getDefaultLanguage()
    }

    private fun getDefaultLanguage(): LanguagesDTO {
        return LanguagesDTO().apply {
            this.title = LanguageCodeEnum.VN.getLanguageName()
            this.locale = LanguageCodeEnum.VN.getLocale()
            this.ma = LanguageCodeEnum.VN.code
        }
    }

    fun getCurrentSiteUrl(context: Context): String {
        return Base.company.value?.code ?: ((PreferenceUtils.getString(
            context,
            PreferenceUtils.KEY_SITE,
            ""
        ) ?: Constant.EMPTY_STRING).toObject<Site>()?.code ?: "")
    }

    fun getListSite(context: Context): MutableList<Site>? {
        if ((Base.listSite.value?.size ?: 0) == 0) {
            val myType = object : TypeToken<MutableList<Site>>() {}.type
            Base.listSite.value = Gson().fromJson<MutableList<Site>>(
                PreferenceUtils.getString(
                    context,
                    PreferenceUtils.KEY_LIST_SITE,
                    ""
                ), myType
            )
        }
        return Base.listSite.value
    }

    fun getTokenUser(context: Context): String {
        return Base.username.value?.tokenLogin ?: ((PreferenceUtils.getString(
            context,
            PreferenceUtils.KEY_ACCOUNT,
            ""
        ) ?: Constant.EMPTY_STRING).toObject<AccountDTO>()?.tokenLogin) ?: ""
    }

    fun getIconFile(fileUrl: String?): Int {
        if (fileUrl != null) {
            if (!TextUtils.isEmpty(fileUrl)) {
                if (fileUrl.contains(Constant.PDF_FORMAT) || fileUrl.contains(Constant.FORMAT_PDF)) {
                    return R.drawable.ic_pdf
                } else if (fileUrl.contains(Constant.DOC_FORMAT) || fileUrl.contains(Constant.DOCX_FORMAT)) {
                    return R.drawable.ic_word
                } else if (fileUrl.contains(Constant.XLS_FORMAT) || fileUrl.contains(Constant.XLSX_FORMAT)) {
                    return R.drawable.ic_excel
                } else if (fileUrl.contains(Constant.PNG_FORMAT)) {
                    return R.drawable.ic_png
                } else if (fileUrl.contains(Constant.JPEG_FORMAT) || fileUrl.contains(Constant.JPG_FORMAT)) {
                    return R.drawable.ic_jpg
                } else if (fileUrl.contains(Constant.ZIP_FORMAT) || fileUrl.contains(Constant.RAR_FORMAT)) {
                    return R.drawable.ic_zip
                } else if (fileUrl.contains(Constant.TXT_FORMAT)) {
                    return R.drawable.ic_txt
                } else if (fileUrl.contains(Constant.PPT_FORMAT) || fileUrl.contains(Constant.PPTX_FORMAT)) {
                    return R.drawable.ic_power_point
                }
            }
        }
        return R.drawable.ic_unknown
    }

    fun openDatePicker(
        context: Context,
        dateFrom: Calendar? = null,
        dateTo: Calendar? = null,
        currentDate: Calendar? = null,
        allowDelete: Boolean? = true,
        callbackCalendar: (Calendar?) -> (Unit)
    ) {
        val selectDate = currentDate ?: Calendar.getInstance()
        val datePickerDialog = if (allowDelete == true) DatePickerWithCancel(
            context,
            DatePickerDialog.OnDateSetListener { datePicker: DatePicker?, selectYear: Int, selectMonth: Int, selectDate: Int ->
                callbackCalendar.invoke(if (selectYear == 0) {
                    null
                } else {
                    Calendar.getInstance().apply {
                        set(selectYear, selectMonth, selectDate)
                    }
                })
            },
            selectDate.get(Calendar.YEAR),
            selectDate.get(Calendar.MONTH),
            selectDate.get(Calendar.DATE)
        ) else {
            DatePickerDialog(

                context,
                DatePickerDialog.OnDateSetListener { datePicker: DatePicker?, selectYear: Int, selectMonth: Int, selectDate: Int ->
                    callbackCalendar.invoke(if (selectYear == 0) {
                        null
                    } else {
                        Calendar.getInstance().apply {
                            set(selectYear, selectMonth, selectDate)
                        }
                    })
                },
                selectDate.get(Calendar.YEAR),
                selectDate.get(Calendar.MONTH),
                selectDate.get(Calendar.DATE)
            )
        }
        if (dateFrom != null)
            datePickerDialog.datePicker.minDate = dateFrom.time.time
        if (dateTo != null)
            datePickerDialog.datePicker.maxDate = dateTo.time.time
        datePickerDialog.show()
    }


    fun isBetween(dateStart: Date?, dateEnd: Date?): Boolean {
        return isBetween(dateStart, dateEnd, Date())
    }

    fun isBetween(dateStart: Date?, dateEnd: Date?, date: Date?): Boolean {
        if (date != null && dateStart != null && dateEnd != null) {
            return date.after(dateStart) && date.before(dateEnd)
        }
        return false
    }

    fun isBetweenOrEqual(dateStart: Date?, dateEnd: Date?, date: Date?): Boolean {
        if (date != null && dateStart != null && dateEnd != null) {
            return (date.after(dateStart) || date.toShow() == dateStart.toShow()) && (date.before(
                dateEnd
            ) || date.toShow() == dateEnd.toShow())
        }
        return false
    }

    fun getDateAddingBy(hours: Int): Date {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")
        val currentDateandTime = sdf.format(Date())

        val date = sdf.parse(currentDateandTime)
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.HOUR, hours)
        return calendar.time
    }

}