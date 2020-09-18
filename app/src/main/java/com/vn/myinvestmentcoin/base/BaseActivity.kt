package com.vn.custom.activity.base

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.vn.custom.util.GeneralUtil
import com.vn.myinvestmentcoin.MIApplication
import com.vn.myinvestmentcoin.R
import com.vn.myinvestmentcoin.customview.BaseCustomView
import com.vn.myinvestmentcoin.dialog.CustomProgressDialog
import com.vn.myinvestmentcoin.enumApp.TypeEventBusEnum
import com.vn.myinvestmentcoin.eventbus.MessageEvent
import kotlinx.android.synthetic.main.toolbar.*
import org.greenrobot.eventbus.EventBus


abstract class BaseActivity : AppCompatActivity() {
    var loadingDialog: CustomProgressDialog? = null
    var tvMessage: TextView? = null
    var mDialogWithMessage: AlertDialog? = null
    var reload = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (isFullScreen()) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            setStatusBarColor()
        }
        super.onCreate(savedInstanceState)
        initDialog()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    open fun eventClickListener() {
        setupUI(window.decorView)
    }

    open fun isFullScreen(): Boolean {
        return true
    }

    open fun getSwipeLayout(): SwipeRefreshLayout? {
        return null
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun setStatusBarGradient(activity: Activity) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = activity.resources.getColor(android.R.color.transparent, null)
    }

    private fun setStatusBarColor() {
        setStatusBarGradient(this)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            getWindow().setStatusBarColor(
//                getResources().getColor(
//                    R.color.toolbar_start_color,
//                    this.getTheme()
//                )
//            );
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().setStatusBarColor(getResources().getColor(R.color.toolbar_start_color));
//        }

    }

    fun setUpActionbar() {
        getMyToolbar()?.let {
            setSupportActionBar(it)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            it.setContentInsetsAbsolute(0, 0)
            supportActionBar?.title = ""
            getTitleToolbar()?.let {
                tvTitle?.text = it
            }
            it.setNavigationOnClickListener {
                handleBackToolbar()
            }

        }
        setMarginTopForToolbar()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu?.size() == 0) {
            getMyToolbar()?.let {
                menuFake?.visibility = View.INVISIBLE
            }
        } else {
            if (menu?.getItem(0)?.isVisible == true) {
                getMyToolbar()?.let {
                    menuFake?.visibility = View.GONE
                }
            } else {
                getMyToolbar()?.let {
                    menuFake?.visibility = View.INVISIBLE
                }
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    open fun setTitleScreen(title: String?) {
        title?.let {
            tvTitle?.text = it
        }
    }

    open fun setMarginTopForToolbar() {
        getMyToolbar()?.let {

            if (it.layoutParams is CollapsingToolbarLayout.LayoutParams) {
                return
            }
            val toolbarParams = if (it.layoutParams is FrameLayout.LayoutParams) {
                it.layoutParams as FrameLayout.LayoutParams
            } else if (it.layoutParams is LinearLayout.LayoutParams) {
                it.layoutParams as LinearLayout.LayoutParams
            } else if (it.layoutParams is AppBarLayout.LayoutParams) {
                it.layoutParams as AppBarLayout.LayoutParams
            } else if (it.layoutParams is ConstraintLayout.LayoutParams) {
                it.layoutParams as ConstraintLayout.LayoutParams
            } else {
                it.layoutParams as RelativeLayout.LayoutParams
            }
            toolbarParams.topMargin = GeneralUtil.getStatusBarHeight(this)
        }

    }

    open fun getMyToolbar(): Toolbar? {
        return null
    }

    open fun getTitleToolbar(): String? {
        return null
    }

    open fun handleBackToolbar() {
        onBackPressed()
    }

    private fun initDialog() {
        loadingDialog = CustomProgressDialog(this, R.style.ProgressDialogDim)
    }

    open fun hideLoading() {
        if (loadingDialog?.isShowing == true) {
            loadingDialog?.dismiss()
        }
    }

    open fun showLoading() {
        showLoading(false)
    }

    open fun showLoading(cancelable: Boolean) {
        if (loadingDialog?.isShowing != true) {
            loadingDialog?.setCancelable(cancelable)
            loadingDialog?.show()
        }
    }

    fun showDialog(message: String?) {
//        DialogUtils.showDialog(this, message)
    }

    fun <T : Throwable> showDialog(error: T) {
//        if (error is NetWorkException || error is ServerException) {
//            DialogUtils.showDialogIcon(this, error)
//        } else {
//            DialogUtils.showDialog(this, error.message)
//        }
    }

    fun showDialog(message: String?, listener: () -> Unit) {
//        DialogUtils.showDialog(this, message, listener)
    }

    fun showConfirmDialog(
        title: String? = getString(R.string.notification),
        message: String?,
        actionYes: (() -> Unit)? = null,
        actionNo: (() -> Unit)? = null
    ) {
//        MessageDialog.getInstance(message ?: "").showWithConfirm(
//            supportFragmentManager,
//            this::class.simpleName ?: ""
//        ) {
//            if (it)
//                actionYes?.invoke()
//            else
//                actionNo?.invoke()
//        }
    }

//    protected fun showDialogWithMessage(message: String) {
//        if (this.tvMessage == null) {
//            val builder = AlertDialog.Builder(this)
//            builder.setCancelable(false) // if you want user to wait for some process to finish,
//            var view = layoutInflater.inflate(R.layout.layout_loading_dialog, null)
//            this.tvMessage = view.findViewById(R.id.tvMessage)
//            this.tvMessage?.text = message
//            builder.setView(view)
//            mDialogWithMessage = builder.create()
//            mDialogWithMessage?.show()
////            MessageDialog(message).showWithMessage(supportFragmentManager, this::class.simpleName ?: "")
//        } else {
//            this.tvMessage?.text = message
//        }
//    }
//
//    protected fun hideDialogWithMesasge() {
//        mDialogWithMessage?.hide()
//    }


    open fun postDelay(toDo: () -> Unit) {
        Handler().postDelayed({ toDo.invoke() }, 1000)
    }

    override fun attachBaseContext(newBase: Context) {
//        super.attachBaseContext(MyContextWrapper.wrap(newBase, Base.currentLanguage?.value?.locale ?: LanguageCodeEnum.VN.getLocale()))
        super.attachBaseContext(MIApplication.instance?.getConfigLocale(newBase))
    }

    fun setupUI(view: View) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {
            view.setOnTouchListener { v, event ->
                hideSoftKeyboard()
                false
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupUI(innerView)
            }
        }
    }

    fun availableUI(view: View): Boolean {

        // Set up touch listener for non-text box views to hide keyboard.
        if (view is BaseCustomView<*>) {
            if (view.hasError()) {
                showSnackBar(view.getMessageError())
                return false
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                var rs = availableUI(innerView)
                if (!rs) {
                    return false
                }
            }
            return true
        }
        return true
    }

    open fun availableUI(): Boolean {
        return availableUI(window.decorView)
    }


    private fun hideSoftKeyboard() {
        var inputMethodManager: InputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        this.currentFocus?.windowToken?.let {
            inputMethodManager.hideSoftInputFromWindow(it, 0)
        }
    }

    fun showSnackBar(messageInt: Int?) {
        showSnackBar(getString(messageInt ?: 0))
    }

    fun showSnackBar(message: String?) {

    }

    fun postEventBusReload() {
        if (reload) {
            EventBus.getDefault().post(MessageEvent().apply {
                this.type = TypeEventBusEnum.RELOAD
            })
        }
    }
}