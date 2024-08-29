package org.okstar.okmsg.volley.bean

import kotlinx.serialization.Serializable

/*
{
    "takes": 0,
    "code": 0,
    "msg": "string",
    "data": {
        "username": "string",
        "tokenType": "string",
        "accessToken": "string",
        "expiresIn": 0,
        "refreshToken": "string",
        "refreshExpiresIn": 0,
        "session_state": "string"
    },
    "extra": {
        "additionalProp1": "string",
        "additionalProp2": "string",
        "additionalProp3": "string"
    }
}
 */
@Serializable
data class LoginInfo(
    val username:String? = "",
    val tokenType:String? = "",
    val accessToken:String? = "",
    val expiresIn:Int? = 0,
    val refreshToken:String? = "",
    val refreshExpiresIn:Int? = 0,
    val session_state: String? = ""
)
@Serializable
data class LoginInfoExtra(
    val additionalProp1:String? = "",
    val additionalProp2:String? = "",
    val additionalProp3:String? = ""
)
@Serializable
data class LoginResponse(
    val takes:Int? = 0,
    val code: Int? = 0,
    val msg: String? = "",
    val data: LoginInfo? = null,
    val extra: LoginInfoExtra? = null
)
