package com.vn.eoffice.base

import android.view.View
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vn.custom.activity.base.BaseMVVMFragment
import com.vn.custom.activity.base.BaseViewModel
import com.vn.eoffice.enumApp.TypeLoadDataEnum

abstract class BaseShimmerFragment<V : ViewDataBinding, M : BaseViewModel> :
    BaseMVVMFragment<V, M>() {

    open fun getShimmerLayout(): View? {
        return null
    }

    open fun getRecyclerView(): View? {
        return null
    }

    open fun showProgress() {
        super.showLoading()
    }

    open fun hideProgress() {
        super.hideLoading()
    }

    override fun showLoading() {
        getRecyclerView()?.visibility = View.GONE
        getTvNoData()?.visibility = View.GONE
        (getShimmerLayout() as? ShimmerFrameLayout)?.startShimmer()
        getShimmerLayout()?.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        getRecyclerView()?.visibility = View.VISIBLE
        (getShimmerLayout() as? ShimmerFrameLayout)?.stopShimmer()
        getShimmerLayout()?.visibility = View.GONE
        getSwipeLayout()?.isRefreshing = false
    }

}