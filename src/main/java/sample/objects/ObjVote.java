package sample.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONObject;

/**
 * Created by Denis on 20.03.2017.
 */
public class ObjVote {
    @SuppressWarnings("MultipleVariablesInDeclaration")
    private int id, voice, userid;
    @SuppressWarnings("MultipleVariablesInDeclaration")
    private String slug, nickname;

    public ObjVote() {
    }

    @JsonCreator
    public ObjVote(
            @JsonProperty("nickname") String nickname,
            @JsonProperty("slug") String slug,
            @JsonProperty("id") int id,
            @JsonProperty("voice") int voice) {
        this.nickname = nickname;
        this.id = id;
        this.userid = userid;
        this.slug = slug;
        this.voice = voice;
    }

    public int getThreadId() {
        return id;
    }

    public int getVoice() {
        return voice;
    }

    public String getNickname() {
        return nickname;
    }

    public int getUserId(){return userid;}

    public String getSlug() {
        return slug;
    }

    public void setThreadId(int id) {
        this.id = id;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setVoice(int voice) {
        this.voice = voice;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public JSONObject getJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("nickname", nickname);
        jsonObject.put("userid", userid);
        jsonObject.put("slug", slug);
        jsonObject.put("voice", voice);
        return jsonObject;
    }
}
