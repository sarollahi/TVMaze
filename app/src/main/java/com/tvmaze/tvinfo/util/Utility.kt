package com.tvmaze.tvinfo.util


class Utility {
    fun fixImageAddress(url:String):String{
        val fixedUrl = url.replace("http", "https")
        return fixedUrl
    }
}