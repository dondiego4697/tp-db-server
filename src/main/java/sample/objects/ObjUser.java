package sample.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONObject;

/**
 * Created by Denis on 14.03.2017.
 */
public class ObjUser {
    @SuppressWarnings("MultipleVariablesInDeclaration")
    private String nickname = "", fullname = "", about = "", email = "";
    private int id;

    public ObjUser() {

    }

    @JsonCreator
    public ObjUser(
            @JsonProperty("nickname") String nickname,
            @JsonProperty("fullname") String fullname,
            @JsonProperty("about") String about,
            @JsonProperty("email") String email) {
        this.id = id;
        this.nickname = nickname;
        this.fullname = fullname;
        this.about = about;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAbout() {
        return about;
    }

    public String getEmail() {
        return email;
    }

    public String getFullname() {
        return fullname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public JSONObject getJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("nickname", nickname);
        jsonObject.put("fullname", fullname);
        jsonObject.put("about", about);
        jsonObject.put("email", email);
        return jsonObject;
    }
}
