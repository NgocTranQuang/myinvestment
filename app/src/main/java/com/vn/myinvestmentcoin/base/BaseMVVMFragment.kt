package com.vn.custom.activity.base

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.vn.eoffice.enumApp.TypeLoadDataEnum
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class BaseMVVMFragment<V : ViewDataBinding, B : BaseViewModel> : BaseFragment() {
    var mViewBinding: V? = null
    var mViewModel: B? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewBinding = DataBindingUtil.inflate(inflater, getLayout(), container, false);
        return mViewBinding?.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (getActivityViewModel() == null) {
            mViewModel = ViewModelProviders.of(this).get(getViewModelClass())
        } else {
            mViewModel = ViewModelProviders.of(getActivityViewModel()!!).get(getViewModelClass())
        }
        context?.let {
            mViewModel?.setContext(it)
        }
        mViewBinding?.lifecycleOwner = this
        setViewModelToViewBinding(mViewModel)
        getData()
        observer()
        eventClickListener()
        initView()
    }

    open fun getActivityViewModel(): FragmentActivity? {
        return null
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

    open fun observer() {
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
                activity?.onBackPressed()
            }
        })
        mViewModel?.exceptionInvoke?.observe(this, Observer {
            onGetDataError(it)
        })
    }

    open fun onGetDataError(it: Throwable?) {

    }

    open fun getViewModelClass(): Class<B> {
        return ((this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<B>)
    }

    override fun onDestroy() {
        setViewModelToViewBinding(null)
        mViewModel = null
        mViewBinding = null
        super.onDestroy()
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

    open fun reloadData() {

    }

    open fun clearData() {

    }

    open fun <T> handleListData(list: MutableList<T>?): MutableList<T> {
        if (list == null || list.size == 0) {
            if(mViewModel?.typeLoadDataEnum == TypeLoadDataEnum.LOAD_MORE){
                showError(false)
            }else {
                showError(true)
            }
            return mutableListOf()
        } else {
            showError(false)
            return list
        }
    }

    override fun hideLoading() {
        super.hideLoading()
        getSwipeLayout()?.isRefreshing = false
    }

    open fun showError(isShow: Boolean) {
        getTvNoData()?.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    open fun getTvNoData(): TextView? = null
}