package sample.sql;

import com.sun.istack.internal.NotNull;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import sample.objects.ObjForum;
import sample.objects.ObjPost;
import sample.rowsmap.PostMapper;
import sample.rowsmap.ThreadMapper;
import sample.support.TransformDate;

/**
 * Created by Denis on 22.03.2017.
 */
public class PostService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public PostService(@NotNull JdbcTemplate jdbcTemplate) {
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
                    final ResponseEntity responseEntity = new UserService(jdbcTemplate).get(objPost.getAuthor());
                    if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                        result.put("author", new JSONObject(responseEntity.getBody()));
                    }
                    break;
                }
                case "forum": {
                    final ResponseEntity responseEntity = new ForumService(jdbcTemplate)
                            .details(objPost.getForum());
                    if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                        result.put("forum", new JSONObject(responseEntity.getBody()));
                    }
                    break;
                }
                case "thread": {
                    final ResponseEntity responseEntity = new ThreadService(jdbcTemplate)
                            .getThreadDetails(String.valueOf(objPost.getThread()));
                    if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                        result.put("thread", new JSONObject(responseEntity.getBody()));
                    }
                    break;
                }
            }
        }
        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }

    public ResponseEntity<String> update(Integer id, ObjPost newPost) {
        final ObjPost objPost;
        try {
            objPost = jdbcTemplate.queryForObject(
                    "SELECT * FROM post WHERE id=?",
                    new Object[]{id}, new PostMapper());
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }

        if (objPost.getEdited()) {
            jdbcTemplate.update("UPDATE post SET message=? WHERE id=?",
                    newPost.getMessage(), objPost.getId());
        } else {
            jdbcTemplate.update("UPDATE post SET message=?, isedited=true WHERE id=?",
                    newPost.getMessage(), objPost.getId());
            objPost.setEdited(true);
        }
        objPost.setMessage(newPost.getMessage());
        objPost.setCreated(TransformDate.transformWithAppend00(objPost.getCreated()));
        return new ResponseEntity<>(objPost.getJson().toString(), HttpStatus.OK);
    }
}
