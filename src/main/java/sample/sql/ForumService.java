package sample.sql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import sample.objects.ObjForum;
import sample.objects.ObjUser;
import sample.rowsmap.ForumMapper;
import sample.rowsmap.UserMapper;

import java.util.List;

/**
 * Created by Denis on 15.03.2017.
 */
public class ForumService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public interface Callback {
        void onSuccess(ResponseEntity<String> responseEntity);

        void onError(ResponseEntity<String> responseEntity);
    }

    public ForumService() {

    }

    public ResponseEntity<String> create(ObjForum objForum) {
        try {
            final String SQLUsers = "SELECT * FROM users WHERE lower(nickname) = ?";
            final ObjUser user = jdbcTemplate.queryForObject(SQLUsers,
                    new Object[]{objForum.getUser().toLowerCase()}, new UserMapper());
            objForum.setUser(user.getNickname());
        } catch (Exception e2) {
           return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }

        final String SQLForum = "SELECT * FROM forum WHERE title = ? or slug= ? or \"user\" =? ";
        final List<ObjForum> forum = jdbcTemplate.query(SQLForum,
                new Object[]{objForum.getTitle(), objForum.getSlug(), objForum.getUser()}, new ForumMapper());

        if (!forum.isEmpty()) {
            return new ResponseEntity<>(forum.get(0).getJson().toString(), HttpStatus.CONFLICT);
        }
        jdbcTemplate.update(
                "INSERT INTO forum (title,\"user\",slug,posts,threads) values(?,?,?,?,?)",
                objForum.getTitle(), objForum.getUser(), objForum.getSlug(), objForum.getPosts(), objForum.getThreads());
        return new ResponseEntity<>(objForum.getJson().toString(), HttpStatus.CREATED);
    }

}
