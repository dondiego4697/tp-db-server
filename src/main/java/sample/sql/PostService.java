package sample.sql;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import sample.objects.ObjForum;
import sample.objects.ObjPost;
import sample.objects.ObjThread;
import sample.objects.ObjUser;
import sample.rowsmap.PostMapper;
import sample.support.TransformDate;

/**
 * Created by Denis on 22.03.2017.
 */
public class PostService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public PostService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ResponseEntity<String> get(Integer id, String related) {
        final ObjPost objPost;
        try {
            objPost = jdbcTemplate.queryForObject(
                    "SELECT * FROM post WHERE id=?",
                    new Object[]{id}, new PostMapper());
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }

        final JSONObject result = new JSONObject();
        objPost.setCreated(TransformDate.transformWithAppend00(objPost.getCreated()));
        result.put("post", objPost.getJson());

        final String[] arrRelated = related.split(",");
        for (String option : arrRelated) {
            switch (option) {
                case "user": {
                    final ObjUser objUser = new UserService(jdbcTemplate).getObjUser(objPost.getAuthor());
                    if (objUser != null) {
                        result.put("author", objUser.getJson());
                    }
                    break;
                }
                case "forum": {
                    final ObjForum objForum = new ForumService(jdbcTemplate).getObjForum(objPost.getForum());
                    if (objForum != null) {
                        result.put("forum", objForum.getJson());
                    }
                    break;
                }
                case "thread": {
                    final ObjThread objThread = new ThreadService(jdbcTemplate).getObjThread(
                            String.valueOf(objPost.getThread()));
                    if (objThread != null) {
                        objThread.setCreated(TransformDate.transformWithAppend00(objThread.getCreated()));
                        result.put("thread", objThread.getJson());
                    }
                    break;
                }
            }
        }
        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }

    public ObjPost getObjPost(Integer id) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM post WHERE id=?",
                    new Object[]{id}, new PostMapper());
        } catch (Exception e) {
            return null;
        }
    }

    public ResponseEntity<String> update(Integer id, ObjPost newPost) {
        final ObjPost objPost;
        if (newPost.getMessage() != null) {
            objPost = this.getObjPost(id);
            if(objPost == null){
                return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            }

            if (!newPost.getMessage().equals(objPost.getMessage())) {
                if (objPost.getEdited()) {
                    jdbcTemplate.update("UPDATE post SET message=? WHERE id=?",
                            newPost.getMessage(), objPost.getId());
                } else {
                    jdbcTemplate.update("UPDATE post SET message=?, isedited=true WHERE id=?",
                            newPost.getMessage(), objPost.getId());
                    objPost.setEdited(true);
                }
                objPost.setMessage(newPost.getMessage());
            }
            objPost.setCreated(TransformDate.transformWithAppend00(objPost.getCreated()));
            return new ResponseEntity<>(objPost.getJson().toString(), HttpStatus.OK);
        } else {
            objPost = this.getObjPost(id);
            if(objPost == null){
                return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            }
            objPost.setCreated(TransformDate.transformWithAppend00(objPost.getCreated()));
            return new ResponseEntity<>(objPost.getJson().toString(), HttpStatus.OK);
        }
    }

    public Integer getCountOfMainPosts(Integer threadId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post WHERE parent=0 AND thread=?",
                new Object[]{threadId}, Integer.class
        );
    }
}
