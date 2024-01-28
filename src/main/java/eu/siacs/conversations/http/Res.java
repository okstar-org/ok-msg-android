package eu.siacs.conversations.http;

import java.io.Serializable;

public class Res<T> implements Serializable {

    //    {"takes":0,"code":0,"msg":null,
    //      "data":{"id":5001,"iso":"CN","username":"3lpvgiSFeh0d",
    //              "nickname":null,"firstName":"","lastName":"",
    //              "avatar":"/assets/images/avatar.jpg","name":"3lpvgiSFeh0d"},"extra":{}}
    private Integer code;
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

    public Integer getCode() {
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

