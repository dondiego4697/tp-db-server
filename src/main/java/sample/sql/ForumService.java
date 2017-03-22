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
import sample.support.TransformDate;

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
                    "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?)",
                    new Object[]{objForum.getUser()}, new UserMapper());
            objForum.setUser(user.getNickname());
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }

        final List<ObjForum> forum = jdbcTemplate.query(
                /*"SELECT * FROM forum WHERE title = ? OR slug= ? OR \"user\" =? "*/
                "SELECT * FROM forum WHERE LOWER(slug)=LOWER(?)",
                new Object[]{objForum.getSlug()/*objForum.getTitle(), objForum.getSlug(), objForum.getUser()*/},
                new ForumMapper());

        if (!forum.isEmpty()) {
            return new ResponseEntity<>(forum.get(0).getJson().toString(), HttpStatus.CONFLICT);
        }
        jdbcTemplate.update(
                "INSERT INTO forum (title,\"user\",slug,posts,threads) VALUES(?,?,?,?,?)",
                objForum.getTitle(), objForum.getUser(), objForum.getSlug(), objForum.getPosts(),
                objForum.getThreads());
        return new ResponseEntity<>(objForum.getJson().toString(), HttpStatus.CREATED);
    }


    public ResponseEntity<String> details(String slug) {
        try {
            final ObjForum forum = jdbcTemplate.queryForObject(
                    "SELECT * FROM forum WHERE LOWER(slug) = LOWER(?)",
                    new Object[]{slug}, new ForumMapper());
            System.out.println(forum.getJson());
            System.out.println(slug);
            return new ResponseEntity<>(forum.getJson().toString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<String> createThread(ObjThread objThread, String slug) {
        try {
            final ObjThread thread = jdbcTemplate.queryForObject(
                    "SELECT * FROM thread WHERE /*LOWER(title)= LOWER(?) OR*/ LOWER(slug)=LOWER(?)",
                    new Object[]{/*objThread.getTitle(),*/ objThread.getSlug()}, new ThreadMapper());
            thread.setCreated(TransformDate.transformWithAppend00(thread.getCreated()));
            return new ResponseEntity<>(thread.getJson().toString(), HttpStatus.CONFLICT);
        } catch (Exception e) {

        }

        ObjForum objForum;
        try {
            jdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE LOWER(nickname)=LOWER(?)",
                    new Object[]{objThread.getAuthor()}, new UserMapper());
            objForum = jdbcTemplate.queryForObject(
                    "SELECT * FROM forum WHERE LOWER(slug)=LOWER(?)",
                    new Object[]{slug}, new ForumMapper());
            objThread.setForum(objForum.getSlug());
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }

        final KeyHolder holder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            final PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO thread (title,author,forum,message,slug," +
                            "votes,created) VALUES (?,?,?,?,?,?,?::timestamptz)", new String[]{"id"});
            ps.setString(1, objThread.getTitle());
            ps.setString(2, objThread.getAuthor());
            ps.setString(3, objThread.getForum());
            ps.setString(4, objThread.getMessage());
            ps.setString(5, objThread.getSlug());
            ps.setInt(6, objThread.getVotes());
            ps.setString(7, objThread.getCreated());
            return ps;
        }, holder);
        objThread.setId((int) holder.getKey());

        jdbcTemplate.update(
                "UPDATE forum SET threads=threads+1 WHERE LOWER(slug)=LOWER(?)",
                slug);


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

            if (desc != null && desc) SQLThreads.append(" AND created <=?::timestamptz ");
            else SQLThreads.append(" AND created >=?::timestamptz ");
        }
        SQLThreads.append(" ORDER BY created ");

        if (desc != null && desc) SQLThreads.append(" DESC ");

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
            } else {
                threads = jdbcTemplate.query(SQLThreads.toString(),
                        new Object[]{slug}, new ThreadMapper());
            }
        }

        final JSONArray result = new JSONArray();
        for (ObjThread objThread : threads) {
            objThread.setCreated(TransformDate.transformWithAppend00(objThread.getCreated()));
            result.put(objThread.getJson());
        }
        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }

    public ResponseEntity<String> getForumUsers(String slug, Integer limit, String since, Boolean desc) {
        final ResponseEntity<String> forum = details(slug);
        if (forum.getStatusCode() == HttpStatus.NOT_FOUND) return forum;

        final StringBuilder query = new StringBuilder(
                "SELECT *, OCTET_LENGTH(LOWER(nickname)) FROM users WHERE nickname IN")
        .append("(SELECT u.nickname FROM users as u FULL OUTER JOIN post as p ")
        .append("ON LOWER(u.nickname)=LOWER(p.author) FULL OUTER JOIN thread as t ")
        .append("ON LOWER(u.nickname)=LOWER(t.author) WHERE LOWER(p.forum)=LOWER(?) ")
        .append("OR LOWER(t.forum)=LOWER(?) GROUP BY u.nickname)");

        if(since!=null){
            if(desc != null && desc) {
                query.append(" AND nickname<'").append(since).append("'");
            } else {
                query.append(" AND nickname>'").append(since).append("'");
            }
        }
        query.append("  ORDER BY nickname");

        if(desc != null && desc) query.append(" DESC");

        if(limit != null){
            query.append(" LIMIT ").append(limit);
        }
        System.out.println(slug);
        System.out.println(query.toString());
        final List<ObjUser> arrObjUser = jdbcTemplate.query(
                query.toString(),
                new Object[]{slug, slug}, new UserMapper());


        final JSONArray result = new JSONArray();
        for (ObjUser objUser : arrObjUser) {
            result.put(objUser.getJson());
        }

        return new ResponseEntity<>(result.toString(), HttpStatus.OK);
    }

}
