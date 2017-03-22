package sample.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONObject;

/**
 * Created by Denis on 14.03.2017.
 */
public class ObjThread {

    @SuppressWarnings("MultipleVariablesInDeclaration")
    private int id, votes = 0;
    @SuppressWarnings("MultipleVariablesInDeclaration")
    private String title = "", author, forum = "", message = "", slug, created = "";

    public ObjThread() {

    }

    @JsonCreator
    public ObjThread(
            @JsonProperty("id") int id,
            @JsonProperty("title") String title,
            @JsonProperty("author") String author,
            @JsonProperty("slug") String slug,
            @JsonProperty("message") String message,
            @JsonProperty("forum") String forum,
            @JsonProperty("votes") int votes,
            @JsonProperty("created") String created) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.forum = forum;
        this.message = message;
        this.slug = slug;
        this.votes = votes;
        this.created = created;
    }

    public boolean isEmpty() {
        if (this.author == null || this.created == null ||
                this.forum == null || this.message == null || this.title == null) {
            return true;
        } else {
            return false;
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public int getId() {
        return id;
    }

    public int getVotes() {
        return votes;
    }

    public String getAuthor() {
        return author;
    }

    public String getCreated() {
        return created;
    }

    public String getForum() {
        return forum;
    }

    public String getMessage() {
        return message;
    }

    public JSONObject getJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("author", author);
        jsonObject.put("created", created);
        jsonObject.put("forum", forum);
        jsonObject.put("id", id);
        jsonObject.put("message", message);
        jsonObject.put("slug", slug);
        jsonObject.put("title", title);
        if (votes != 0) jsonObject.put("votes", votes);
        return jsonObject;
    }
}
