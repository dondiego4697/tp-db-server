package sample.objects;

import org.json.JSONObject;

/**
 * Created by Denis on 22.03.2017.
 */
public class ObjService {
    @SuppressWarnings("MultipleVariablesInDeclaration")
    private int forum, post, thread, user;

    public ObjService() {
        this.forum = 0;
        this.post = 0;
        this.thread = 0;
        this.user = 0;
    }

    public ObjService(int forum, int post, int thread, int user) {
        this.forum = forum;
        this.post = post;
        this.thread = thread;
        this.user = user;
    }

    public int getForum() {
        return forum;
    }

    public int getPost() {
        return post;
    }

    public int getThread() {
        return thread;
    }

    public int getUser() {
        return user;
    }

    public void setThread(int thread) {
        this.thread = thread;
    }

    public void setForum(int forum) {
        this.forum = forum;
    }

    public void setPost(int post) {
        this.post = post;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public JSONObject getJson() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("forum", forum);
        jsonObject.put("post", post);
        jsonObject.put("thread", thread);
        jsonObject.put("user", user);
        return jsonObject;
    }
}
