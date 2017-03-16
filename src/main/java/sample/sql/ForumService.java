package sample.sql;

import com.sun.istack.internal.NotNull;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import sample.objects.ObjForum;
import sample.objects.ObjThread;
import sample.objects.ObjUser;
import sample.rowsmap.ForumMapper;
import sample.rowsmap.ThreadMapper;
import sample.rowsmap.UserMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Denis on 15.03.2017.
 */
public class ForumService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ForumService(@NotNull JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ResponseEntity<String> create(ObjForum objForum) {
        try {
            final ObjUser user = jdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE LOWER(nickname) = ?",
                    new Object[]{objForum.getUser().toLowerCase()}, new UserMapper());
            objForum.setUser(user.getNickname());
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }

        final List<ObjForum> forum = jdbcTemplate.query(
                "SELECT * FROM forum WHERE title = ? OR slug= ? OR \"user\" =? ",
                new Object[]{objForum.getTitle(), objForum.getSlug(), objForum.getUser()}, new ForumMapper());

        if (!forum.isEmpty()) {
            return new ResponseEntity<>(forum.get(0).getJson().toString(), HttpStatus.CONFLICT);
        }
        jdbcTemplate.update(
                "INSERT INTO forum (title,\"user\",slug,posts,threads) VALUES(?,?,?,?,?)",
                objForum.getTitle(), objForum.getUser(), objForum.getSlug(), objForum.getPosts(), objForum.getThreads());
        return new ResponseEntity<>(objForum.getJson().toString(), HttpStatus.CREATED);
    }


    public ResponseEntity<String> details(String slug) {
        try {
            final ObjForum forum = jdbcTemplate.queryForObject(
                    "SELECT * FROM forum WHERE LOWER(slug) = ?",
                    new Object[]{slug.toLowerCase()}, new ForumMapper());
            return new ResponseEntity<>(forum.getJson().toString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<String> createThread(ObjThread objThread, String slug) {
        try {
            final ObjThread thread = jdbcTemplate.queryForObject(
                    "SELECT * FROM thread WHERE LOWER(title)= ?",
                    new Object[]{objThread.getTitle().toLowerCase()}, new ThreadMapper());
            return new ResponseEntity<>(thread.getJson().toString(), HttpStatus.CONFLICT);
        } catch (Exception e) {
        }

        ObjForum objForum;
        try {
            jdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE LOWER(nickname)=?",
                    new Object[]{objThread.getAuthor().toLowerCase()}, new UserMapper());
            objForum = jdbcTemplate.queryForObject(
                    "SELECT * FROM forum WHERE LOWER(slug)=?",
                    new Object[]{objThread.getForum().toLowerCase()}, new ForumMapper());
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }

        System.out.println("SLUG = " + objForum.getSlug());

        final KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            final PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO thread (title,author,forum,message,slug," +
                            "votes,created) VALUES (?,?,?,?,?,?,?::timestamptz)", new String[]{"id"});
            ps.setString(1, objThread.getTitle());
            ps.setString(2, objThread.getAuthor());
//            ps.setString(3, objThread.getForum());
            ps.setString(3, objForum.getSlug());
            ps.setString(4, objThread.getMessage());
            ps.setString(5, objThread.getSlug());
            ps.setInt(6, objThread.getVotes());
            ps.setString(7, objThread.getCreated());
            return ps;
        }, holder);
        objThread.setId((int) holder.getKey());
        return new ResponseEntity<>(objThread.getJson().toString(), HttpStatus.CREATED);
    }


    public ResponseEntity<String> getThreads(String slug, Integer limit, String since, Boolean desc) {
        final ResponseEntity<String> forum = details(slug);
        if (forum.getStatusCode() == HttpStatus.NOT_FOUND) return forum;

        final StringBuilder SQLThreads = new StringBuilder(
                "SELECT * FROM thread WHERE forum=?");
        if (since != null) {
            final StringBuilder time = new StringBuilder(since);
            time.replace(10, 11, " ");
            since = time.toString();
            SQLThreads.append(" AND created >=?::timestamptz ");
        }
        SQLThreads.append(" ORDER BY created ");

        if (desc) SQLThreads.append(" DESC ");

        List<ObjThread> threads;
        if (limit != null) {
            SQLThreads.append(" LIMIT ?");
            if (since != null) {
                threads = jdbcTemplate.query(SQLThreads.toString(),
                        new Object[]{slug, since, limit}, new ThreadMapper());
            } else {
                threads = jdbcTemplate.query(SQLThreads.toString(),
                        new Object[]{slug, limit}, new ThreadMapper());
            }
        } else {
            if (since != null) {
                threads = jdbcTemplate.query(SQLThreads.toString(),
                        new Object[]{slug, since}, new ThreadMapper());
            } else threads = jdbcTemplate.query(SQLThreads.toString(),
                    new Object[]{slug}, new ThreadMapper());
        }

        final JSONArray result = new JSONArray();
        for (ObjThread objThread : threads) {
            final StringBuilder time = new StringBuilder(objThread.getCreated());
            time.replace(10, 11, "T");
            time.append(":00");
            objThread.setCreated(time.toString());
            result.put(objThread.getJson());
        }
        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }
}
