package sample.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Created by Denis on 14.03.2017.
 */
public class ObjForum {

    @SuppressWarnings("MultipleVariablesInDeclaration")
    private int posts = 0, threads = 0;
    @SuppressWarnings("MultipleVariablesInDeclaration")
    private String title = "", user, slug;

    public ObjForum() {

    }

    @JsonCreator
    public ObjForum(
            @JsonProperty("title") String title,
            @JsonProperty("user") String user,
            @JsonProperty("slug") String slug,
            @JsonProperty("threads") int threads,
            @JsonProperty("posts") int posts) {
        this.title=title;
        this.user = user;
        this.slug = slug;
        this.threads = threads;
        this.posts= posts;
    }

    public int getPosts() {
        return posts;
    }

    public int getThreads() {
        return threads;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public String getUser() {
        return user;
    }

    public void setPosts(int posts) {
        this.posts = posts;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public JSONObject getJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("title",title);
        jsonObject.put("user", user);
        jsonObject.put("slug", slug);
        jsonObject.put("posts", posts);
        jsonObject.put("threads", threads);
        return jsonObject;
    }
}
