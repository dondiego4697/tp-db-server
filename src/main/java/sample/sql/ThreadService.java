package sample.sql;

import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import sample.objects.*;
import sample.rowsmap.PostMapper;
import sample.rowsmap.ThreadMapper;
import sample.rowsmap.VoteMapper;
import sample.support.ObjSlugOrId;
import sample.support.ValueConverter;
import sample.support.TransformDate;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * Created by Denis on 20.03.2017.
 */

@Service
public class ThreadService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    UserService userService;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ResponseEntity<ArrayList<ObjPost>> createPosts(ArrayList<ObjPost> arrObjPost, ObjThread objThread) {

        final Integer threadID = objThread.getId();


        final Timestamp now = new Timestamp(System.currentTimeMillis());
        //final JSONArray result = new JSONArray();

        Integer maxId = jdbcTemplate.queryForObject("SELECT max(id) FROM post", Integer.class);
        maxId = maxId == null ? 0 : maxId;
        Integer currId = maxId;

        //TODO create
        int rootsCount = 0;
        List<Object[]> postList = new ArrayList<>();
        for (ObjPost objPost : arrObjPost) {
            objPost.setForum(objThread.getForum());
            objPost.setThread(threadID);

            if (objPost.getParent() != 0) {
                    try {
                        final List<ObjPost> posts = jdbcTemplate.query(
                                "SELECT * FROM post WHERE id=? AND thread=?",
                                new Object[]{objPost.getParent(), threadID}, new PostMapper());
                        if (posts.isEmpty()) {
                            return new ResponseEntity<>(new ArrayList(), HttpStatus.CONFLICT);
                        }
                    } catch (Exception e) {
                        return new ResponseEntity<>(new ArrayList(), HttpStatus.CONFLICT);
                    }
            }

            final ObjUser objUser = userService.getObjUser(objPost.getAuthor());
            if(objUser == null){
                return new ResponseEntity<>(new ArrayList(), HttpStatus.NOT_FOUND);
            }
            objPost.setUserid(objUser.getId());

            if (objPost.getParent() == 0) {
                final Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM post WHERE parent=0 AND thread=?",
                        new Object[]{objPost.getThread()}, Integer.class
                );
                final String path = ValueConverter.toHex(count + rootsCount);
                objPost.setPath(path);

                rootsCount++;
            } else {
                final String prevPath = jdbcTemplate.queryForObject(
                        "SELECT path FROM post WHERE id = ?;",
                        new Object[]{objPost.getParent()}, String.class
                );

                currId++;
                //objPost.setPath('*'+prevPath);
                objPost.setPath(prevPath+'.'+ValueConverter.toHex(currId));
            }

            objPost.setCreated(now);

            postList.add(new Object[]{
                    objPost.getParent(),
                    objPost.getAuthor(),
                    objPost.getMessage(),
                    objPost.getEdited(),
                    objPost.getForum(),
                    objPost.getThread(),
                    objPost.getPath(),
                    objPost.getCreated(),
                    objPost.getUserid()
            });
        }

        jdbcTemplate.batchUpdate("INSERT INTO post (parent,author,message,isEdited,forum,thread,path,created, userid) " +
                "VALUES (?,?,?,?,?,?,?,?::timestamp with time zone,?)", postList);
        List<Integer> idsList = jdbcTemplate.queryForList("SELECT id FROM post WHERE id > ? ORDER BY id", new Object[]{maxId}, Integer.class);

        IntStream.range(0, arrObjPost.size()).boxed()
                .forEach(i -> {
                    arrObjPost.get(i).setId(idsList.get(i));
                });

        return new ResponseEntity<>(arrObjPost, HttpStatus.CREATED);
    }

    public void addInLinkUserForum(String forumSlug, List<Integer> userIds, int chunkSize) {
        //System.out.println(userNames.toString());
        final List<Object[]> totalList = userIds.stream()
                .distinct()
                .map(id -> new Object[]{forumSlug, id})
                .collect(Collectors.toList());
        final List<List<Object[]>> chunkLists = Lists.partition(totalList, chunkSize);
        chunkLists.forEach(this::addUser);
    }

    private void addUser(List<Object[]> list) {
        final String sql = "INSERT INTO link_user_forum (forum_slug, userid) VALUES (?,?) ON CONFLICT DO NOTHING";
        boolean finished = false;
        while (!finished) {
            try {
                jdbcTemplate.batchUpdate(sql, list);
                finished = true;
            } catch (DeadlockLoserDataAccessException e) {
                System.out.println("DEADLOCK!!!");
            }
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void incrementPosts(String slug, Integer size) {
        final String sql = "UPDATE forum SET posts = posts+" + size +" WHERE LOWER(slug)=LOWER(?)";
        jdbcTemplate.update(sql, slug);
    }

    public ResponseEntity<String> vote(ObjVote objVote, String slug_or_id) {
        final ObjThread result;

        final ObjUser objUser = userService.getObjUser(objVote.getNickname());
        if (objUser == null) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
        objVote.setUserid(objUser.getId());

        ObjThread objThread = this.getObjThread(slug_or_id);
        if (objThread == null) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }


        if (objVote.getVoice() == 1) {
            vote(objVote, objThread.getId());
        } else {
            unvote(objVote, objThread.getId());
        }

        result = jdbcTemplate.queryForObject(
                "SELECT * FROM thread WHERE id =?",
                new Object[]{objThread.getId()}, new ThreadMapper());

        result.setCreated(TransformDate.transformWithAppend00(result.getCreated()));
        return new ResponseEntity<>(result.getJson().toString(), HttpStatus.OK);
    }

    public void vote(ObjVote objVote, Integer threadId) throws DataAccessException {
        final List<ObjVote> objVoteList = jdbcTemplate.query(
                "SELECT * FROM vote WHERE(id, userid)=(?,?)",
                new Object[]{threadId, objVote.getUserId()}, new VoteMapper());

        String sql = "";
        if (!objVoteList.isEmpty() && objVoteList.get(0).getVoice() == -1) {
            sql = "UPDATE thread SET votes = votes + 2 WHERE id = ?; " +
                    "UPDATE vote SET voice = 1 WHERE id = ? AND userid = ?";
        } else if(objVoteList.isEmpty()){
            sql = "UPDATE thread SET votes = votes + 1 WHERE id = ?; " +
                    "INSERT INTO vote (id, userid, voice) VALUES (?, ?, 1)";
        }
        if(!sql.equals(""))
            voteDeadlock(sql, threadId, objVote.getUserId());
    }

    public void unvote(ObjVote objVote, Integer threadId) throws DataAccessException {
        final List<ObjVote> objVoteList = jdbcTemplate.query(
                "SELECT * FROM vote WHERE(id, userid)=(?,?)",
                new Object[]{threadId, objVote.getUserId()}, new VoteMapper());
            String sql ="";
            if (!objVoteList.isEmpty() && objVoteList.get(0).getVoice() == 1) {
                sql = "UPDATE thread SET votes = votes - 2 WHERE id = ?; " +
                        "UPDATE vote SET voice = -1 WHERE id = ? AND userid = ?";
            } else if(objVoteList.isEmpty()){
                sql = "UPDATE thread SET votes = votes - 1 WHERE id = ?; " +
                        "INSERT INTO vote (id, userid, voice) VALUES (?, ?, -1)";
            }
            if(!sql.equals(""))
                voteDeadlock(sql, threadId, objVote.getUserId());
    }

    private void voteDeadlock(String sql, int threadId, int userid) {
        try {
            boolean finished = false;
            while (!finished) {
                jdbcTemplate.update(sql, threadId, threadId, userid);
                finished = true;
            }

        } catch (DeadlockLoserDataAccessException e){
            System.out.println("VOTING_DEADLOCK!!!");
        }
    }

    public ResponseEntity<String> getThreadDetails(String slug_or_id) {
        final ObjSlugOrId objSlugOrId = new ObjSlugOrId(slug_or_id);
        try {
            if (objSlugOrId.getFlag()) {
                final ObjThread result = this.getObjThreadBySlug(objSlugOrId.getSlug());
                result.setCreated(TransformDate.transformWithAppend00(result.getCreated()));
                return new ResponseEntity<>(result.getJson().toString(), HttpStatus.OK);
            }
            final ObjThread result = this.getObjThreadById(objSlugOrId.getId());
            result.setCreated(TransformDate.transformWithAppend00(result.getCreated()));
            return new ResponseEntity<>(result.getJson().toString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<String> getThreadPosts(String slug_or_id, Integer limit,
                                                 String sort, Boolean desc, Integer marker) {
        final ObjSlugOrId objSlugOrId = new ObjSlugOrId(slug_or_id);
        final ObjThread objThread;

        final StringBuilder postQuery = new StringBuilder(
                "SELECT * FROM post WHERE thread=");
        if (objSlugOrId.getFlag()) {
            objThread = this.getObjThreadBySlug(objSlugOrId.getSlug());

        } else {
            objThread = this.getObjThreadById(objSlugOrId.getId());
        }
        if (objThread != null) {
            final ObjThread thread = objThread;
            postQuery.append(thread.getId());

            List<ObjPost> posts = null;
            if (sort == null) sort = "flat";
            switch (sort) {
                case "flat": {
                    postQuery.append(" ORDER BY created");
                    if (desc != null && desc) postQuery.append(" DESC");
                    postQuery.append(" , id");
                    if (desc != null && desc) postQuery.append(" DESC");
                    postQuery.append(" LIMIT ").append(limit.toString());
                    postQuery.append(" OFFSET ").append(marker.toString());
                    //System.out.println("query=" + postQuery);
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
                    //System.out.println("query=" + postQuery);
                    break;
                }
                case "parent_tree": {
                    if (limit != null) {
                        if (desc != null && !desc) {
                            final Integer maxIds = new PostService(jdbcTemplate).
                                    getCountOfMainPosts(thread.getId());

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
                            final Integer maxIds = new PostService(jdbcTemplate).
                                    getCountOfMainPosts(thread.getId());

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
                    //System.out.println("query=" + postQuery);
                    break;
                }
            }
            try {
                posts = jdbcTemplate.query(postQuery.toString(), new PostMapper());
            } catch (Exception e) {

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
                    //objPost.setCreated(TransformDate.transformWithAppend0300(objPost.getCreated()));
                    objPost.setCreated(objPost.getCreated());
                    resultArray.put(objPost.getJson());
                }
            }
            result.put("posts", resultArray);
            return new ResponseEntity<>(result.toString(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
    }

    public ObjThread getObjThreadBySlug(String slug) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM thread WHERE LOWER(slug)=LOWER(?)",
                    new Object[]{slug}, new ThreadMapper());
        } catch (Exception e) {
            return null;
        }
    }

    public ObjThread getObjThreadById(Integer id) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM thread WHERE id=?",
                    new Object[]{id}, new ThreadMapper());
        } catch (Exception e) {
            return null;
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
        } catch (Exception e) {
            return null;
        }
        return objThread;
    }

    public ResponseEntity<String> updateThread(ObjThread newData, String slug_or_id) {
        final ObjSlugOrId objSlugOrId = new ObjSlugOrId(slug_or_id);
        if (this.getThreadDetails(slug_or_id).getStatusCode() == HttpStatus.OK) {
            if (newData.getMessage() != null && newData.getTitle() != null) {
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
            } else if (newData.getMessage() != null && newData.getTitle() == null) {
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
            } else if (newData.getMessage() == null && newData.getTitle() != null) {
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
