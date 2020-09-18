package com.vn.myinvestmentcoin.eventbus

import com.vn.myinvestmentcoin.enumApp.TypeEventBusEnum


class MessageEvent {
    var type : TypeEventBusEnum?=null
    var value : Any?=null
}