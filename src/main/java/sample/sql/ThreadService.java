package sample.sql;

import com.sun.istack.internal.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import sample.objects.ObjPost;
import sample.objects.ObjThread;
import sample.objects.ObjVote;
import sample.rowsmap.PostMapper;
import sample.rowsmap.ThreadMapper;
import sample.rowsmap.VoteMapper;
import sample.support.ObjSlugOrId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Denis on 20.03.2017.
 */
public class ThreadService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ThreadService(@NotNull JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ResponseEntity<String> createPost(ArrayList<ObjPost> arrObjPost, String slug_or_id) {
        for (ObjPost objPost : arrObjPost) {
            final ObjThread objThread;
            try {
                objThread = jdbcTemplate.queryForObject(
                        "SELECT * FROM thread WHERE LOWER(author) = ?",
                        new Object[]{objPost.getAuthor().toLowerCase()}, new ThreadMapper());
            } catch (Exception e) {
                return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            }

            objPost.setForum(objThread.getForum());
            objPost.setThread(objThread.getId());

            final KeyHolder holder = new GeneratedKeyHolder();
            final ObjSlugOrId objSlugOrId = new ObjSlugOrId(slug_or_id);
            if (!objSlugOrId.getFlag()) {
                objPost.setId(objSlugOrId.getId());
                jdbcTemplate.update(connection -> {
                    final PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO post (id,parent,author,message,isEdited,forum,thread) " +
                                    "VALUES (?,?,?,?,?,?,?)", new String[]{"created"});
                    ps.setInt(1, objPost.getId());
                    ps.setInt(2, objPost.getParent());
                    ps.setString(3, objPost.getAuthor());
                    ps.setString(4, objPost.getMessage());
                    ps.setBoolean(5, objPost.getEdited());
                    ps.setString(6, objPost.getForum());
                    ps.setInt(7, objPost.getThread());
                    return ps;
                }, holder);
                final StringBuilder created = new StringBuilder(
                        (holder.getKeys().get("created").toString()));
                created.replace(10, 11, "T");
                created.append("+03:00");
                objPost.setCreated(created.toString());
                System.out.println("(String)holder.getKeys().get(\"created\")     " + holder.getKeys().get("created").toString());
            } else {
                jdbcTemplate.update(connection -> {
                    final PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO post (parent,author,message,isEdited,forum,thread) " +
                                    "VALUES (?,?,?,?,?,?)", new String[]{"id", "created"});
                    ps.setInt(1, objPost.getParent());
                    ps.setString(2, objPost.getAuthor());
                    ps.setString(3, objPost.getMessage());
                    ps.setBoolean(4, objPost.getEdited());
                    ps.setString(5, objPost.getForum());
                    ps.setInt(6, objPost.getThread());
                    return ps;
                }, holder);

                objPost.setId((int) holder.getKeys().get("id"));
                final StringBuilder created = new StringBuilder((holder.getKeys().
                        get("created").toString()));
                created.replace(10, 11, "T");
                created.append("+03:00");
                objPost.setCreated(created.toString());
                System.out.println("(String)holder.getKeys().get(\"created\")     " + holder.getKeys().get("created").toString());
            }
        }

        final JSONArray result = new JSONArray();
        for (ObjPost objPost2 : arrObjPost) {
            result.put(objPost2.getJson());
        }
        return new ResponseEntity<>(result.toString(), HttpStatus.CREATED);
    }

    public ResponseEntity<String> vote(ObjVote objVote, String slug_or_id) {
        final ObjSlugOrId objSlugOrId = new ObjSlugOrId(slug_or_id);
        final ObjThread result;
        if (!objSlugOrId.getFlag()) {
            objVote.setThreadId(objSlugOrId.getId());
            final List<ObjVote> objVoteList = jdbcTemplate.query(
                    "SELECT * FROM vote WHERE(id, LOWER(nickname))=(?,LOWER(?))",
                    new Object[]{objVote.getThreadId(), objVote.getNickname()}, new VoteMapper());

            if (objVoteList.isEmpty()) {
                if (objVote.getVoice() == 1)
                    jdbcTemplate.update(
                            "UPDATE thread SET votes=votes+1 WHERE id=?",
                            new Object[]{objVote.getThreadId()});
                else {
                    jdbcTemplate.update(
                            "UPDATE thread SET votes=votes-1 WHERE id=?",
                            new Object[]{objVote.getThreadId()});
                }
                result = jdbcTemplate.queryForObject(
                        "SELECT * FROM thread WHERE id =?",
                        new Object[]{objVote.getThreadId()}, new ThreadMapper());

                jdbcTemplate.update(
                        "INSERT INTO vote (id,nickname,voice,slug) VALUES(?,?,?,?)",
                        objVote.getThreadId(), objVote.getNickname(), objVote.getVoice(), result.getSlug());
            } else {
                jdbcTemplate.update(
                        "UPDATE vote SET voice=? WHERE id=?",
                        objVote.getVoice(), objVote.getThreadId());
                if ((objVote.getVoice() == -1) && (objVoteList.get(0).getVoice() == 1))
                    jdbcTemplate.update(
                            "UPDATE thread SET votes=votes-2 WHERE id=?",
                            new Object[]{objVote.getThreadId()});

                if ((objVote.getVoice() == 1) && (objVoteList.get(0).getVoice() == -1))
                    jdbcTemplate.update(
                            "UPDATE thread SET votes=votes+2 WHERE id=?",
                            new Object[]{objVote.getThreadId()});

                result = jdbcTemplate.queryForObject(
                        "SELECT * FROM thread WHERE id =?",
                        new Object[]{objVote.getThreadId()}, new ThreadMapper());
            }
            final StringBuilder time = new StringBuilder(result.getCreated());
            time.replace(10, 11, "T");
            time.append(":00");
            result.setCreated(time.toString());
            return new ResponseEntity<>(result.getJson().toString(), HttpStatus.OK);

        } else {
            objVote.setSlug(objSlugOrId.getSlug());

            final List<ObjVote> objVoteList = jdbcTemplate.query(
                    "SELECT * FROM vote WHERE (slug,LOWER(nickname))=(?,LOWER(?))",
                    new Object[]{objVote.getSlug(), objVote.getNickname()}, new VoteMapper());

            if (objVoteList.isEmpty()) {
                jdbcTemplate.update(
                        "INSERT INTO vote (slug,nickname,voice) VALUES(?,?,?)",
                        objVote.getSlug(), objVote.getNickname(), objVote.getVoice());
                if (objVote.getVoice() == 1)
                    jdbcTemplate.update("UPDATE thread SET votes=votes+1 WHERE slug=?", objSlugOrId.getSlug());
                else {
                    jdbcTemplate.update("UPDATE thread SET votes=votes-1 WHERE slug=?", objSlugOrId.getSlug());
                }

                result = jdbcTemplate.queryForObject(
                        "SELECT * FROM thread WHERE slug =?",
                        new Object[]{objVote.getSlug()}, new ThreadMapper());

                jdbcTemplate.update(
                        "UPDATE vote SET id=? WHERE slug=?",
                        result.getId(), result.getSlug());
            } else {
                jdbcTemplate.update(
                        "UPDATE vote SET voice=? WHERE slug=?",
                        objVote.getVoice(), objVote.getSlug());

                if ((objVote.getVoice() == -1) && (objVoteList.get(0).getVoice() == 1)) {
                    jdbcTemplate.update(
                            "UPDATE thread SET votes=votes-2 WHERE slug=?", objSlugOrId.getSlug());
                }
                if ((objVote.getVoice() == 1) && (objVoteList.get(0).getVoice() == -1)) {
                    jdbcTemplate.update(
                            "UPDATE thread SET votes=votes+2 WHERE slug=?", objSlugOrId.getSlug());
                }
                result = jdbcTemplate.queryForObject(
                        "SELECT * FROM thread WHERE slug =?",
                        new Object[]{objVote.getSlug()}, new ThreadMapper());
            }
            final StringBuilder time = new StringBuilder(result.getCreated());
            time.replace(10, 11, "T");
            time.append(":00");
            result.setCreated(time.toString());
            return new ResponseEntity<>(result.getJson().toString(), HttpStatus.OK);
        }
    }

    public ResponseEntity<String> getThreadDetails(String slug_or_id) {
        final ObjSlugOrId objSlugOrId = new ObjSlugOrId(slug_or_id);
        try {
            if (objSlugOrId.getFlag()) {
                final ObjThread result = jdbcTemplate.queryForObject(
                        "SELECT * FROM thread WHERE LOWER(slug)=LOWER(?)",
                        new Object[]{objSlugOrId.getSlug()}, new ThreadMapper());
                final StringBuilder time = new StringBuilder(result.getCreated());
                time.replace(10, 11, "T");
                time.append(":00");
                result.setCreated(time.toString());
                return new ResponseEntity<>(result.getJson().toString(), HttpStatus.OK);
            }
            final ObjThread result = jdbcTemplate.queryForObject(
                    "SELECT * FROM thread WHERE id=?",
                    new Object[]{objSlugOrId.getId()}, new ThreadMapper());
            final StringBuilder time = new StringBuilder(result.getCreated());
            time.replace(10, 11, "T");
            time.append(":00");
            result.setCreated(time.toString());
            return new ResponseEntity<>(result.getJson().toString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<String> getThreadPosts(String slug_or_id, Integer limit,
                                                 String sort, Boolean desc, Integer marker) {
        final ObjSlugOrId objSlugOrId = new ObjSlugOrId(slug_or_id);
        final ObjThread thread;

        final StringBuilder postRequestSQL = new StringBuilder("SELECT * FROM post WHERE thread=");
        if (objSlugOrId.getFlag()) {
            thread = jdbcTemplate.queryForObject("SELECT * FROM thread WHERE slug=?",
                    new Object[]{objSlugOrId.getSlug()}, new ThreadMapper());

            postRequestSQL.append(objSlugOrId.getSlug() + " ");
        } else {
            thread = jdbcTemplate.queryForObject("SELECT * FROM thread WHERE id=?",
                    new Object[]{objSlugOrId.getId()}, new ThreadMapper());

            postRequestSQL.append(objSlugOrId.getId() + " ");
        }


        List<ObjPost> posts = null;
        if (sort != null) {
            switch (sort) {
                case "flat": {
                    postRequestSQL.append(" ORDER BY created ");
                    if (desc != null && desc) postRequestSQL.append(" DESC ");
                    final Integer sumLimitAndMarker = limit + marker;
                    postRequestSQL.append("LIMIT ").append(sumLimitAndMarker.toString());
                    posts = jdbcTemplate.query(postRequestSQL.toString(), new PostMapper());

                    final JSONObject result = new JSONObject();
                    if (marker > posts.size()) result.put("marker", marker.toString());
                    else result.put("marker", sumLimitAndMarker.toString());

                    final JSONArray resultArray = new JSONArray();
                    for (ObjPost objPost : posts) {
                        final StringBuilder time = new StringBuilder(objPost.getCreated());
                        time.replace(10, 11, "T");
                        time.append(":00");
                        objPost.setCreated(time.toString());
                        resultArray.put(objPost.getJson());
                    }
                    result.put("posts", resultArray);
                    return new ResponseEntity<>(result.toString(), HttpStatus.OK);
                }
                case "tree": {

                    break;
                }
                case "parent_tree": {

                    break;
                }
            }
        }
        return new ResponseEntity<>("", HttpStatus.NO_CONTENT);
    }
}
