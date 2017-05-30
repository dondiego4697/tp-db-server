package sample.sql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import sample.objects.ObjService;

/**
 * Created by Denis on 22.03.2017.
 */
@Service
public class DBService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /*public DBService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }*/

    public ResponseEntity<String> getInfo() {
        final Integer forum = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM forum",
                new Object[]{}, Integer.class);
        final Integer post = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM post",
                new Object[]{}, Integer.class);
        final Integer thread = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM thread",
                new Object[]{}, Integer.class);
        final Integer user = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users",
                new Object[]{}, Integer.class);

        return new ResponseEntity<>(new ObjService(forum, post, thread, user).getJson().toString(), HttpStatus.OK);
    }

    public ResponseEntity<String> clear() {
        jdbcTemplate.update(
                "DELETE FROM users; DELETE FROM post; DELETE FROM thread; DELETE FROM forum; DELETE FROM vote;"
                );

        return new ResponseEntity<>(new ObjService().getJson().toString(), HttpStatus.OK);
    }
}
