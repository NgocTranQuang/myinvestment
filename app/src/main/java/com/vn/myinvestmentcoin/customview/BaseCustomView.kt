package com.vn.myinvestmentcoin.customview

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.vn.myinvestmentcoin.R

open abstract class BaseCustomView<D : ViewDataBinding> : LinearLayout {
    var binding: D? = null
    var ta: TypedArray? = null
    var mesageError: String? = null

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView(context, attrs)
    }

    open fun initView(context: Context?, attrs: AttributeSet?) {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            getLayoutRs(),
            this,
            true
        );
        ta = context?.obtainStyledAttributes(attrs, R.styleable.ViewTitle)
        mesageError = ta?.getString(R.styleable.ViewTitle_message_error)
    }

    abstract fun getLayoutRs(): Int


    abstract fun conditionError(): Boolean

    fun getMessageError(): String? {
        return mesageError
    }
    fun hasError(): Boolean {
        return (!mesageError.isNullOrBlank() && conditionError())
    }
}