package eu.siacs.conversations.volley.request

import android.util.Log
import com.kymjs.rxvolley.RxVolley
import com.kymjs.rxvolley.client.HttpCallback
import com.kymjs.rxvolley.client.HttpParams
import eu.siacs.conversations.Config
import eu.siacs.conversations.volley.bean.LoginInfo
import eu.siacs.conversations.volley.bean.LoginInfoExtra
import eu.siacs.conversations.volley.bean.LoginResponse
import eu.siacs.conversations.volley.parser.VolleyParser.parser


object VolleyUtil {
fun doLogin(host:String, username:String, password:String, block:(loginInfo:LoginInfo?, loginInfoExtra:LoginInfoExtra?,code:Int,msg:String)->Unit){
    val baseUrl = "$host/api/auth/passport/signIn"

        val params = HttpParams().apply {
            put("iso","CN")
            put("grantType","password")
            put("account",username)
            put("password",password)
            put("rememberMe","true")
        }

    Log.i(Config.LOGTAG, "Login in account: $baseUrl account:$username password:$password")
        RxVolley.jsonPost(baseUrl, params, object : HttpCallback() {
            override fun onSuccess(success: String) {
                Log.i(Config.LOGTAG,"success: $success")
                super.onSuccess(success)

                val serializerBean = success.parser(LoginResponse.serializer())
                block.invoke(serializerBean.data, serializerBean.extra, serializerBean.code, serializerBean.msg)
            }

            override fun onFailure(errorNo: Int, strMsg: String?) {

                Log.i(Config.LOGTAG,"errorCode: $errorNo message: $strMsg")
                super.onFailure(errorNo, strMsg)

            }
        })
    }
}