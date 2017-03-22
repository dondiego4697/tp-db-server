package sample.sql;

import com.sun.istack.internal.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import sample.objects.*;
import sample.rowsmap.PostMapper;
import sample.rowsmap.ThreadMapper;
import sample.rowsmap.VoteMapper;
import sample.support.ObjSlugOrId;
import sample.support.ValueConverter;
import sample.support.TransformDate;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Denis on 20.03.2017.
 */
public class ThreadService {

    public static final int NO_PARENT = 0;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ThreadService(@NotNull JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ResponseEntity<String> createPost(ArrayList<ObjPost> arrObjPost, String slug_or_id) {
        final ObjThread objThread;
        final ObjSlugOrId objSlugOrId = new ObjSlugOrId(slug_or_id);
        if (!objSlugOrId.getFlag()) {
            try {
                objThread = jdbcTemplate.queryForObject(
                        "SELECT * FROM thread WHERE id=?",
                        new Object[]{objSlugOrId.getId()}, new ThreadMapper());
            } catch (Exception e) {
                return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            }
        } else {
            try {
                objThread = jdbcTemplate.queryForObject(
                        "SELECT * FROM thread WHERE LOWER(slug) = LOWER(?)",
                        new Object[]{objSlugOrId.getSlug()}, new ThreadMapper());
            } catch (Exception e) {
                return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            }
        }


        for (ObjPost objPost : arrObjPost) {
            objPost.setForum(objThread.getForum());
            objPost.setThread(objThread.getId());

            if (objPost.getParent() != 0) {
                try {
                    final List<ObjPost> posts = jdbcTemplate.query(
                            "SELECT * FROM post WHERE id=? AND thread=?",
                            new Object[]{objPost.getParent(), objThread.getId()}, new PostMapper());
                    System.out.println("adasad=" + posts.size());
                    if (posts.isEmpty()) {
                        return new ResponseEntity<>("", HttpStatus.CONFLICT);
                    }
                } catch (Exception e) {
                    return new ResponseEntity<>("", HttpStatus.CONFLICT);
                }
            }

            final ResponseEntity responseEntity = new UserService(jdbcTemplate).get(objPost.getAuthor());
            if (responseEntity.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            }


            final KeyHolder holder = new GeneratedKeyHolder();
            if (objPost.getParent() == 0) {
                final Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM post WHERE parent=0 AND thread=?",
                        new Object[]{objPost.getThread()}, Integer.class
                );
                final String path = ValueConverter.toHex(count);
                objPost.setPath(path);

                jdbcTemplate.update(connection -> {
                    final PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO post (parent,author,message,isEdited,forum,thread,path) " +
                                    "VALUES (?,?,?,?,?,?,?)", new String[]{"id", "created"});
                    ps.setInt(1, objPost.getParent());
                    ps.setString(2, objPost.getAuthor());
                    ps.setString(3, objPost.getMessage());
                    ps.setBoolean(4, objPost.getEdited());
                    ps.setString(5, objPost.getForum());
                    ps.setInt(6, objPost.getThread());
                    ps.setString(7, objPost.getPath());
                    return ps;
                }, holder);

                objPost.setId((int) holder.getKeys().get("id"));
                objPost.setCreated(TransformDate.transformWithAppend0300(
                        holder.getKeys().get("created").toString()));
            } else {
                final String prevPath = jdbcTemplate.queryForObject(
                        "SELECT path FROM post WHERE id = ?;",
                        new Object[]{objPost.getParent()}, String.class
                );

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

                final int id = (int) holder.getKeys().get("id");
                objPost.setId(id);
                objPost.setCreated(TransformDate.transformWithAppend0300(
                        holder.getKeys().get("created").toString()));

                final String path = prevPath + '.' + ValueConverter.toHex(id);
                objPost.setPath(path);
                jdbcTemplate.update(
                        "UPDATE post SET path=? WHERE id=?",
                        new Object[]{path, id});
            }
        }

        jdbcTemplate.update(
                "UPDATE forum SET posts=posts+" + arrObjPost.size() + " WHERE LOWER(slug)=LOWER(?)",
                objThread.getForum());

        final JSONArray result = new JSONArray();
        for (ObjPost objPost2 : arrObjPost) {
            result.put(objPost2.getJson());
        }
        return new ResponseEntity<>(result.toString(), HttpStatus.CREATED);
    }

    public ResponseEntity<String> vote(ObjVote objVote, String slug_or_id) {
        final ObjSlugOrId objSlugOrId = new ObjSlugOrId(slug_or_id);
        final ObjThread result;

        if (new UserService(jdbcTemplate).get(objVote.getNickname()).getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }

        if (new ThreadService(jdbcTemplate).getThreadDetails(slug_or_id).getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }


        if (!objSlugOrId.getFlag()) {
            objVote.setThreadId(objSlugOrId.getId());

            /*final List<ObjThread> objThreadList = jdbcTemplate.query(
                    "SELECT * FROM thread WHERE id=?",
                    new Object[]{objSlugOrId.getId()}, new ThreadMapper());*/

            /*final List<ObjUser> objUserList = jdbcTemplate.query(
                    "SELECT * FROM users WHERE LOWER(nickname)=LOWER(?)",
                    new Object[]{objVote.getNickname()}, new UserMapper());*/

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

            result.setCreated(TransformDate.transformWithAppend00(result.getCreated()));
            return new ResponseEntity<>(result.getJson().toString(), HttpStatus.OK);
        } else {
            objVote.setSlug(objSlugOrId.getSlug());

          /*  final List<ObjThread> objThreadList = jdbcTemplate.query(
                    "SELECT * FROM thread WHERE LOWER(slug)=LOWER(?)",
                    new Object[]{objSlugOrId.getSlug()}, new ThreadMapper());

            final List<ObjUser> objUserList = jdbcTemplate.query(
                    "SELECT * FROM users WHERE LOWER(nickname)=LOWER(?)",
                    new Object[]{objVote.getNickname()}, new UserMapper());*/

            final List<ObjVote> objVoteList = jdbcTemplate.query(
                    "SELECT * FROM vote WHERE (LOWER(slug),LOWER(nickname))=(LOWER(?),LOWER(?))",
                    new Object[]{objVote.getSlug(), objVote.getNickname()}, new VoteMapper());

            if (objVoteList.isEmpty()) {
                jdbcTemplate.update(
                        "INSERT INTO vote (slug,nickname,voice) VALUES(?,?,?)",
                        objVote.getSlug(), objVote.getNickname(), objVote.getVoice());
                if (objVote.getVoice() == 1)
                    jdbcTemplate.update("UPDATE thread SET votes=votes+1 WHERE LOWER(slug)=LOWER(?)",
                            objSlugOrId.getSlug());
                else {
                    jdbcTemplate.update("UPDATE thread SET votes=votes-1 WHERE LOWER(slug)=LOWER(?)",
                            objSlugOrId.getSlug());
                }

                result = jdbcTemplate.queryForObject(
                        "SELECT * FROM thread WHERE LOWER(slug) =LOWER(?)",
                        new Object[]{objVote.getSlug()}, new ThreadMapper());

                jdbcTemplate.update(
                        "UPDATE vote SET id=? WHERE LOWER(slug)=LOWER(?)",
                        result.getId(), result.getSlug());
            } else {
                jdbcTemplate.update(
                        "UPDATE vote SET voice=? WHERE LOWER(slug)=LOWER(?)",
                        objVote.getVoice(), objVote.getSlug());

                if ((objVote.getVoice() == -1) && (objVoteList.get(0).getVoice() == 1)) {
                    jdbcTemplate.update(
                            "UPDATE thread SET votes=votes-2 WHERE LOWER(slug)=LOWER(?)",
                            objSlugOrId.getSlug());
                }
                if ((objVote.getVoice() == 1) && (objVoteList.get(0).getVoice() == -1)) {
                    jdbcTemplate.update(
                            "UPDATE thread SET votes=votes+2 WHERE LOWER(slug)=LOWER(?)",
                            objSlugOrId.getSlug());
                }
                result = jdbcTemplate.queryForObject(
                        "SELECT * FROM thread WHERE LOWER(slug) =LOWER(?)",
                        new Object[]{objVote.getSlug()}, new ThreadMapper());
            }
            result.setCreated(TransformDate.transformWithAppend00(result.getCreated()));
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
                result.setCreated(TransformDate.transformWithAppend00(result.getCreated()));
                return new ResponseEntity<>(result.getJson().toString(), HttpStatus.OK);
            }
            final ObjThread result = jdbcTemplate.queryForObject(
                    "SELECT * FROM thread WHERE id=?",
                    new Object[]{objSlugOrId.getId()}, new ThreadMapper());
            result.setCreated(TransformDate.transformWithAppend00(result.getCreated()));
            return new ResponseEntity<>(result.getJson().toString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<String> getThreadPosts(String slug_or_id, Integer limit,
                                                 String sort, Boolean desc, Integer marker) {
        final ObjSlugOrId objSlugOrId = new ObjSlugOrId(slug_or_id);
        final List<ObjThread> threadList;

        final StringBuilder postQuery = new StringBuilder(
                "SELECT * FROM post WHERE thread=");
        if (objSlugOrId.getFlag()) {
            threadList = jdbcTemplate.query("SELECT * FROM thread WHERE LOWER(slug)=LOWER(?)",
                    new Object[]{objSlugOrId.getSlug()}, new ThreadMapper());

        } else {
            threadList = jdbcTemplate.query("SELECT * FROM thread WHERE id=?",
                    new Object[]{objSlugOrId.getId()}, new ThreadMapper());

        }
        if (!threadList.isEmpty()) {
            final ObjThread thread = threadList.get(0);
            postQuery.append(thread.getId());

            List<ObjPost> posts = null;
            if (sort == null) sort = "flat";
            switch (sort) {
                case "flat": {
                    postQuery.append(" ORDER BY created");
                    if (desc != null && desc) postQuery.append(" DESC");
                    postQuery.append(" LIMIT ").append(limit.toString());
                    postQuery.append(" OFFSET ").append(marker.toString());
                    break;
                }
                case "tree": {
                    postQuery.append(" ORDER BY LEFT(path,6)");
                    if (desc != null && desc) {
                        postQuery.append(" DESC");
                        postQuery.append(", path DESC");
                    }
                    if (desc != null && !desc) {
                        postQuery.append(", path ASC");
                    }

                    postQuery.append(" LIMIT ").append(limit.toString());
                    postQuery.append(" OFFSET ").append(marker.toString());
                    break;
                }
                case "parent_tree": {
                    if (limit != null) {
                        if (desc != null && !desc) {
                            final Integer maxIds = jdbcTemplate.queryForObject(
                                    "SELECT COUNT(*) FROM post WHERE parent=0 AND thread=?",
                                    new Object[]{thread.getId()}, Integer.class
                            );

                            if ((maxIds - limit - marker) < 0) {
                                postQuery.append(" AND path >= '").append(ValueConverter.toHex(marker))
                                        .append("'");
                            } else {
                                postQuery.append(" AND path >= '").
                                        append(ValueConverter.toHex(marker)).append("'")
                                        .append(" AND path < '").append(
                                        ValueConverter.toHex(marker + limit)).append("'");
                            }

                        } else {
                            final Integer maxIds = jdbcTemplate.queryForObject(
                                    "SELECT COUNT(*) FROM post WHERE parent=0 AND thread=?",
                                    new Object[]{thread.getId()}, Integer.class
                            );

                            if ((maxIds - limit - marker) < 0) {
                                final int top = maxIds - marker;
                                postQuery.append(" AND path >= ").append("'0'")
                                        .append(" AND path < '").append(
                                        ValueConverter.toHex(top)).append("'");
                            } else {
                                int top = maxIds - marker;
                                final int bottom = maxIds - limit - marker;
                                postQuery.append(" AND path >= '").
                                        append(ValueConverter.toHex(bottom)).append("'")
                                        .append(" AND path < '").append(
                                        ValueConverter.toHex(top)).append("'");
                            }
                        }
                    }
                    postQuery.append(" ORDER BY LEFT(path,6)");
                    if (desc != null && desc) {
                        postQuery.append(" DESC");
                        postQuery.append(", path DESC");
                    }
                    if (desc != null && !desc) {
                        postQuery.append(", path ASC");
                    }
                    break;
                }
            }
            System.out.println(postQuery.toString());
            try {
                posts = jdbcTemplate.query(postQuery.toString(), new PostMapper());
            } catch (Exception e) {
                System.out.println(e.toString());
            }


            final JSONObject result = new JSONObject();
            if (posts != null && posts.isEmpty()) {
                result.put("marker", marker.toString());
            } else {
                final Integer SumLimAndMarker = limit + marker;
                result.put("marker", SumLimAndMarker.toString());
            }

            final JSONArray resultArray = new JSONArray();
            if (posts != null) {
                for (ObjPost objPost : posts) {
                    objPost.setCreated(TransformDate.transformWithAppend00(objPost.getCreated()));
                    resultArray.put(objPost.getJson());
                }
            }
            result.put("posts", resultArray);
            return new ResponseEntity<>(result.toString(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
    }

    public ObjThread getObjThread(String slug_or_id) {
        final ObjSlugOrId objSlugOrId = new ObjSlugOrId(slug_or_id);
        final ObjThread objThread;
        try {
            if (!objSlugOrId.getFlag()) {
                objThread = jdbcTemplate.queryForObject("SELECT * FROM thread WHERE id=?",
                        new Object[]{objSlugOrId.getId()}, new ThreadMapper());
            } else {
                objThread = jdbcTemplate.queryForObject("SELECT * FROM thread WHERE LOWER(slug)=LOWER(?)",
                        new Object[]{objSlugOrId.getSlug()}, new ThreadMapper());
            }
        } catch (Exception e){
            return null;
        }
        return objThread;
    }

    public ResponseEntity<String> updateThread(ObjThread newData, String slug_or_id) {
        final ObjSlugOrId objSlugOrId = new ObjSlugOrId(slug_or_id);

        if (this.getThreadDetails(slug_or_id).getStatusCode() == HttpStatus.OK) {
            if(newData.getMessage()!=null && newData.getTitle()!=null){
                try {
                    if (!objSlugOrId.getFlag()) {
                        jdbcTemplate.update("UPDATE thread SET message=?, title=? WHERE id=?",
                                newData.getMessage(), newData.getTitle(), objSlugOrId.getId());
                    } else {
                        jdbcTemplate.update("UPDATE thread SET message=?, title=? WHERE LOWER(slug)=LOWER(?)",
                                newData.getMessage(), newData.getTitle(), objSlugOrId.getSlug());
                    }
                } catch (Exception e) {
                    return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
                }
            } else if(newData.getMessage()!=null && newData.getTitle()==null){
                try {
                    if (!objSlugOrId.getFlag()) {
                        jdbcTemplate.update("UPDATE thread SET message=? WHERE id=?",
                                newData.getMessage(), objSlugOrId.getId());
                    } else {
                        jdbcTemplate.update("UPDATE thread SET message=? WHERE LOWER(slug)=LOWER(?)",
                                newData.getMessage(), objSlugOrId.getSlug());
                    }
                } catch (Exception e) {
                    return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
                }
            } else if(newData.getMessage()==null && newData.getTitle()!=null){
                try {
                    if (!objSlugOrId.getFlag()) {
                        jdbcTemplate.update("UPDATE thread SET  title=? WHERE id=?",
                                newData.getTitle(), objSlugOrId.getId());
                    } else {
                        jdbcTemplate.update("UPDATE thread SET title=? WHERE LOWER(slug)=LOWER(?)",
                                newData.getTitle(), objSlugOrId.getSlug());
                    }
                } catch (Exception e) {
                    return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
                }
            }

            final ObjThread objThread = this.getObjThread(slug_or_id);
            if (objThread != null) {
                newData = objThread;
                newData.setCreated(TransformDate.transformWithAppend00(newData.getCreated()));
            }

            return new ResponseEntity<>(newData.getJson().toString(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }

    }
}
