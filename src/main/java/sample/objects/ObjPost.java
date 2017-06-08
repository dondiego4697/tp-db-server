package sample.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONObject;
import sample.support.TransformDate;

import java.sql.Timestamp;

/**
 * Created by Denis on 20.03.2017.
 */
public class ObjPost {
    @SuppressWarnings("MultipleVariablesInDeclaration")
    private int id, parent, thread, userid;
    @SuppressWarnings("MultipleVariablesInDeclaration")
    private String author, message, forum, path;
    private boolean isEdited;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private Timestamp created;

    public ObjPost(){

    }

    @JsonCreator
    public ObjPost(
            @JsonProperty("id") int id,
            @JsonProperty("parent") int parent,
            @JsonProperty("author") String author,
            @JsonProperty("message") String message,
            @JsonProperty("thread") int thread,
            @JsonProperty("isEdited") boolean isEdited,
            @JsonProperty("forum") String forum,
            @JsonProperty("path") String path,
            @JsonProperty("created") Timestamp created) {
        this.id = id;
        this.parent=parent;
        this.author =author;
        this.message = message;
        this.thread = thread;
        this.isEdited= isEdited;
        this.path= path;
        this.forum=forum;
        this.created=created;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public int getUserid() {
        return userid;
    }

    public String getMessage() {
        return message;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getForum() {
        return forum;
    }

    public int getId() {
        return id;
    }

    public int getParent() {
        return parent;
    }

    public int getThread() {
        return thread;
    }

    public String getAuthor() {
        return author;
    }

    public Timestamp getCreated() {
        return created;
    }

    public Boolean getEdited(){
        return isEdited;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public void setThread(int thread) {
        this.thread = thread;
    }
    public JSONObject getJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("author",author);
        jsonObject.put("message", message);
        jsonObject.put("parent", parent);
        jsonObject.put("isEdited", isEdited);
        jsonObject.put("thread", thread);
        jsonObject.put("forum", forum);
        jsonObject.put("created", TransformDate.transformWithAppend0300(created.toString()));
        return jsonObject;
    }
}
