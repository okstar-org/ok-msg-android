package eu.siacs.conversations.entities;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class SignUpResult  implements Serializable {
    private Long userId;
    private String username;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "SignUpResult{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                '}';
    }
}
