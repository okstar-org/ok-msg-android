package org.okstar.okmsg.volley.request

import android.util.Log
import com.kymjs.rxvolley.RxVolley
import com.kymjs.rxvolley.client.HttpCallback
import com.kymjs.rxvolley.client.HttpParams
import eu.siacs.conversations.Config
import org.okstar.okmsg.volley.bean.LoginInfo
import org.okstar.okmsg.volley.bean.LoginInfoExtra
import org.okstar.okmsg.volley.bean.LoginResponse
import org.okstar.okmsg.volley.parser.VolleyParser.parser
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


object VolleyUtil {
fun doLogin(host:String, username:String, password:String, block:(loginInfo: LoginInfo?, loginInfoExtra: LoginInfoExtra?, code:Int, msg:String)->Unit){
    val baseUrl = "$host/api/auth/passport/signIn"

    val loginJson = Json.encodeToString(
        LoginBody.serializer(),
        LoginBody(
            iso = "CN",
            grantType = "password",
            account = username,
            password = password,
            rememberMe = true
        )
    )

        val params = HttpParams().apply {
            putJsonParams(loginJson)
        }

    Log.i(Config.LOGTAG, "Login in account: $baseUrl body:$loginJson" )
        RxVolley.jsonPost(baseUrl, params, object : HttpCallback() {
            override fun onSuccess(success: String) {
                Log.i(Config.LOGTAG,"success: $success")
                super.onSuccess(success)

                val serializerBean = success.parser(LoginResponse.serializer())
                block.invoke(serializerBean.data, serializerBean.extra, serializerBean.code?:0, serializerBean.msg?:"")
            }

            override fun onFailure(errorNo: Int, strMsg: String?) {

                Log.i(Config.LOGTAG,"errorCode: $errorNo message: $strMsg")
                super.onFailure(errorNo, strMsg)

            }
        })
    }
}

@Serializable
data class LoginBody(
    val iso:String,
    val grantType:String,
    val account:String,
    val password: String,
    val rememberMe:Boolean)