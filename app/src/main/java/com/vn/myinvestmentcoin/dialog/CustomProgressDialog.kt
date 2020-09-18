package com.vn.myinvestmentcoin.dialog

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.vn.myinvestmentcoin.R

class CustomProgressDialog : AlertDialog {
    constructor(context: Context, theme: Int) : super(context, theme)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.progress_dialog_layout)
    }
}