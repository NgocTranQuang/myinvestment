package com.vn.custom.activity.base

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.vn.custom.util.GeneralUtil
import com.vn.eoffice.customview.BaseCustomView
import com.vn.eoffice.model.base.NetWorkException
import com.vn.eoffice.model.base.ServerException
import com.vn.eoffice.util.DialogUtils
import kotlinx.android.synthetic.main.toolbar.*

abstract class BaseFragment : Fragment() {

    abstract fun getLayout(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getLayout(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMarginTopForToolbar()
        initView()
    }

    open fun eventClickListener() {
        setupUI(view)
    }

    open fun getSwipeLayout(): SwipeRefreshLayout? {
        return null
    }

    open fun initView() {

    }

    open fun hideLoading() {
        (activity as BaseActivity).hideLoading()
    }

    open fun showLoading() {
        (activity as BaseActivity).showLoading()
    }

    fun showDialog(message: String?) {
        activity?.let {
            DialogUtils.showDialog(it, message)
        }
    }

    fun <T : Throwable> showDialog(error: T) {
        activity?.let {
            if (error is NetWorkException || error is ServerException) {
                DialogUtils.showDialogIcon(it, error)
            } else {
                DialogUtils.showDialog(it, error.message)
            }
        }
    }

    fun showDialog(message: String?, listener: () -> Unit) {
        activity?.let {
            DialogUtils.showDialog(it, message, listener)
        }
    }

    protected fun setMarginTopForToolbar() {
        getMyToolbar()?.let {
            (activity as BaseActivity).setSupportActionBar(it as? Toolbar)
            val toolbarParams = if (it.layoutParams is FrameLayout.LayoutParams) {
                it.layoutParams as FrameLayout.LayoutParams
            } else if (it.layoutParams is LinearLayout.LayoutParams) {
                it.layoutParams as LinearLayout.LayoutParams
            } else if (it.layoutParams is AppBarLayout.LayoutParams) {
                it.layoutParams as AppBarLayout.LayoutParams
            } else if (it.layoutParams is RelativeLayout.LayoutParams) {
                it.layoutParams as RelativeLayout.LayoutParams
            } else {
                it.layoutParams as ConstraintLayout.LayoutParams
            }
            toolbarParams.topMargin = GeneralUtil.getStatusBarHeight(activity!!)
            getTitleToolbar()?.let {
                (activity as BaseActivity).supportActionBar?.title = ""
                tvTitle?.text = it
            }
            getLogoNavigation()?.let { drawable ->
                (it as? Toolbar)?.setNavigationIcon(drawable)
                (it as? Toolbar)?.setNavigationOnClickListener {
                    activity?.onBackPressed()
                }
            }
        }

    }

    open fun getLogoNavigation(): Int? {
        return null
    }

    open fun getMyToolbar(): View? {
        return null
    }

    open fun getTitleToolbar(): String? {
        return null
    }

    fun setupUI(view: View?) {
        view?.let {

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
            for (i in 0 until  view.childCount) {
                val innerView = view.getChildAt(i)
                var rs = availableUI(innerView)
                if(!rs){
                    return false
                }
            }
            return true
        }
        return true
    }

    fun availableUI(): Boolean {
        return availableUI(view!!)
    }

    open fun showSnackBar(message: String?) {
        (activity as? BaseActivity)?.showSnackBar(message)
    }

    private fun hideSoftKeyboard() {
        var inputMethodManager: InputMethodManager =
            context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        activity?.currentFocus?.windowToken?.let {
            inputMethodManager.hideSoftInputFromWindow(it, 0)
        }

    }

}