package eu.siacs.conversations.entities;

import androidx.annotation.NonNull;

import eu.siacs.conversations.Config;
import eu.siacs.conversations.xmpp.Jid;

public class AccountInfo {
    /**
     * {"id":5001,"iso":"CN",
     * "username":"3lpvgiSFeh0d",
     * "nickname":null,
     * "firstName":"",
     * "lastName":"",
     * "avatar":"/assets/images/avatar.jpg",
     * "name":"3lpvgiSFeh0d"}
     */
    Long id;
    String iso;
    String username;
    String nickname;
    String name;
    String avatar;

    public Jid getJid(){
        return Jid.ofEscaped(username, Config.DOMAIN_LOCK, null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "AccountInfo{" +
                "id=" + id +
                ", iso='" + iso + '\'' +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", name='" + name + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }
}
