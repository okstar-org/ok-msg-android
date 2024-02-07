package eu.siacs.conversations.stack;

import java.io.Serializable;

public class Res<T>   {

    //    {"takes":0,"code":0,"msg":null,
    //      "data":{"id":5001,"iso":"CN","username":"3lpvgiSFeh0d",
    //              "nickname":null,"firstName":"","lastName":"",
    //              "avatar":"/assets/images/avatar.jpg","name":"3lpvgiSFeh0d"},"extra":{}}
    private int code = -1;
    private String msg;
    private T data;

    public boolean success() {
        return code == 0;
    }

    public T getData() {
        return data;
    }

    public String getMsg() {
        return msg;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "Res{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}

