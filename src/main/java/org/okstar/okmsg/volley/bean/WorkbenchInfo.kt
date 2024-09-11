package org.okstar.okmsg.volley.bean

import kotlinx.serialization.Serializable

@Serializable
data class WorkbenchInfo(val list: MutableList<WorkbenchBean>? = mutableListOf())

@Serializable
data class WorkbenchBean(
    val type:String? = "",
    val uuid: String? = "",
    val no:String? = "",
    val name:String? = "",
    val avatar:String? = "",
    val descr:String? = "",
    val author:String? = "",
    val email:String? = "",
    val homePage:String? = "",
    val providerId:Int? = 0,
    val introduceId:Int? = 0
)

@Serializable
data class WorkbenchResponse(
    val takes:Int? = 0,
    val code: Int? = 0,
    val msg: String? = "",

    val data:WorkbenchInfo? = null
)