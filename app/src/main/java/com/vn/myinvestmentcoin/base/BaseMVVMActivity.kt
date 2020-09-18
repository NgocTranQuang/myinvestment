package com.vn.custom.activity.base

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.vn.eoffice.enumApp.TypeLoadDataEnum
import com.vn.eoffice.util.LanguagesUtils
import java.lang.reflect.ParameterizedType

abstract class BaseMVVMActivity<V : ViewDataBinding, B : BaseViewModel> : BaseActivity() {
    var mViewBinding: V? = null
    var mViewModel: B? = null

    abstract fun getLayout(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = DataBindingUtil.setContentView(this, getLayout())
        getViewModelClass()?.let {
            mViewModel = ViewModelProviders.of(this).get(it)
            mViewModel?.setContext(this)
        }
        mViewBinding?.lifecycleOwner = this
        setViewModelToViewBinding(mViewModel)
        initView()
        setUpActionbar()
        getData()
        observer()
        eventClickListener()
    }

    open fun initView() {

    }

    open fun getData() {

    }

    override fun eventClickListener() {
        super.eventClickListener()
        getSwipeLayout()?.setOnRefreshListener {
            mViewModel?.typeLoadDataEnum = TypeLoadDataEnum.REFRESH
            refreshData()
        }
    }

    open fun refreshData() {
        getData()
    }

    protected open fun observer() {
        mViewModel?.showLoading?.observe(this, Observer {
            if (it == true) {
                showLoading()
            } else {
                hideLoading()
            }
        })
        mViewModel?.showDialogWarning?.observe(this, Observer {
            showDialog(it)
        })
        mViewModel?.showDialogThenAutoBack?.observe(this, Observer {
            showDialog(it) {
                finish()
            }
        })
        mViewModel?.exceptionInvoke?.observe(this, Observer {
            onGetDataError(it)
        })
    }

    open fun onGetDataError(it: Throwable?) {

    }

    open fun getViewModelClass(): Class<B>? {
        return ((this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as? Class<B>)
    }

    override fun onDestroy() {
        setViewModelToViewBinding(null)
        mViewModel = null
        mViewBinding = null
        super.onDestroy()
    }

    override fun hideLoading() {
        super.hideLoading()
        getSwipeLayout()?.isRefreshing = false
    }

    protected fun setViewModelToViewBinding(value: B?) {
        try {
            mViewBinding?.let { vb ->
                mViewModel?.let { vm ->
                    val method = vb.javaClass.getDeclaredMethod("setViewModel", vm.javaClass)
                    method.invoke(vb, value)
                }
            }
        } catch (ex: Throwable) {
        }
    }


}