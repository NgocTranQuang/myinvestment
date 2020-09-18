package com.vn.eoffice.base

import android.view.View
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import com.facebook.shimmer.ShimmerFrameLayout
import com.vn.custom.activity.base.BaseMVVMActivity
import com.vn.custom.activity.base.BaseViewModel
import com.vn.eoffice.enumApp.TypeLoadDataEnum

abstract class BaseShimmerActivity<V : ViewDataBinding, M : BaseViewModel> :
    BaseMVVMActivity<V, M>() {

    abstract open fun getShimmerLayout(): ShimmerFrameLayout?

    abstract open fun getTvNoData(): TextView?

    abstract open fun getRecyclerView(): View?

    override fun showLoading() {
        getRecyclerView()?.visibility = View.GONE
        getTvNoData()?.visibility = View.GONE
        getShimmerLayout()?.startShimmer()
        getShimmerLayout()?.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        getRecyclerView()?.visibility = View.VISIBLE
        getShimmerLayout()?.stopShimmer()
        getShimmerLayout()?.visibility = View.GONE
        getSwipeLayout()?.isRefreshing = false
    }

    fun showProgress(cancelable: Boolean = true) {
        super.showLoading(cancelable)
    }

    fun hideProgress() {
        super.hideLoading()
    }

    open fun <T> handleListData(list: MutableList<T>?): MutableList<T> {
        if (list == null || list.size == 0) {
            if (mViewModel?.typeLoadDataEnum != TypeLoadDataEnum.LOAD_MORE) {
                showError(true)
            }
            return mutableListOf()
        } else {
            showError(false)
            return list
        }
    }

    open fun showError(isShow: Boolean) {
        getTvNoData()?.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun onGetDataError(it: Throwable?) {
        super.onGetDataError(it)
        hideLoading()
    }
}