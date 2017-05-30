package sample.sql;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Denis on 20.03.2017.
 */
public class ThreadService {

    public static final int NO_PARENT = 0;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ThreadService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer getMaxId() {
        final Integer id = jdbcTemplate.queryForObject("SELECT max(id) FROM post", Integer.class);
        return id == null ? 0 : id;
    }

    public List<Integer> getPostIdsAfterId(Integer id) {
        return jdbcTemplate.queryForList("SELECT id FROM post WHERE id > ? ORDER BY id", new Object[]{id}, Integer.class);
    }

    /*public boolean checkPosts(List<Integer> idsList, int threadId) {
        final ArrayList<Object> params = new ArrayList<>();
        params.add(threadId);
        params.addAll(idsList);
        final String query = "SELECT COUNT(*) FROM post WHERE thread = ? AND id in ( " +
                String.join(", ", Collections.nCopies(idsList.size(), "?")) + " )";
        final Integer count = jdbcTemplate.queryForObject(query,
                params.toArray(), Integer.class);
        System.out.println("COUNT="+count + " " + idsList.size());
        return count.equals(idsList.size());
    }

    private boolean checkPostParents(ArrayList<ObjPost> postsArr, int threadId) {
        final List<Integer> parentIdsList = postsArr.stream()
                .map(ObjPost::getParent)
                .filter(parentId -> parentId != null && !parentId.equals(0))
                .distinct()
                .collect(Collectors.toList());
        boolean check1 = parentIdsList.isEmpty();
        boolean check2 = true;
        if(!check1) {
            check2 = checkPosts(parentIdsList, threadId);
        }
        System.out.println("RESULT="+check1 + " " + check2);

        return check1 || check2 *//*parentIdsList.isEmpty() || checkPosts(parentIdsList, threadId)*//*;
    }*/


    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ResponseEntity<String> createPosts(ArrayList<ObjPost> arrObjPost, String slug_or_id) {
        final ObjThread objThread;

        final ObjSlugOrId objSlugOrId = new ObjSlugOrId(slug_or_id);
        if (!objSlugOrId.getFlag()) {
            try {
                objThread = this.getObjThreadById(objSlugOrId.getId());
                if (objThread == null) {
                    return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
                }
            } catch (Exception e) {
                return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            }
        } else {
            try {
                objThread = this.getObjThreadBySlug(objSlugOrId.getSlug());
                if (objThread == null) {
                    return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
                }
            } catch (Exception e) {
                return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            }
        }

        final Integer threadID = objThread.getId();


        final Timestamp now = new Timestamp(System.currentTimeMillis());
        final JSONArray result = new JSONArray();

        //TODO create
        int rootsCount = 0;
        List<Object[]> postList = new ArrayList<>();
        for (ObjPost objPost : arrObjPost) {
            objPost.setForum(objThread.getForum());
            objPost.setThread(threadID);

           /* Boolean check = checkPostParents(arrObjPost, objThread.getId());
            System.out.println("FINAL CHECK = " + check);*/

           /* if (!checkPostParents(arrObjPost, objThread.getId())) {
                return new ResponseEntity<>("", HttpStatus.CONFLICT);
            }*/
            System.out.println("PARENT="+objPost.getParent() + ", THREAD="+threadID);
            if (objPost.getParent() != 0) {
                    try {
                        final List<ObjPost> posts = jdbcTemplate.query(
                                "SELECT * FROM post WHERE id=? AND thread=?",
                                new Object[]{objPost.getParent(), threadID}, new PostMapper());
                        if (posts.isEmpty()) {
                            System.out.println("CONFLICT!!!!!!!!!!!!!!!!!!!!");
                            return new ResponseEntity<>("", HttpStatus.CONFLICT);
                        }
                    } catch (Exception e) {
                        System.out.println("ERROR post parent = " + e);
                        return new ResponseEntity<>("", HttpStatus.CONFLICT);
                    }
            }

            final ResponseEntity responseEntity = new UserService(jdbcTemplate).get(objPost.getAuthor());
            if (responseEntity.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            }


            //final KeyHolder holder = new GeneratedKeyHolder();

            if (objPost.getParent() == 0) {
                final Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM post WHERE parent=0 AND thread=?",
                        new Object[]{objPost.getThread()}, Integer.class
                );
                final String path = ValueConverter.toHex(count + rootsCount);
                objPost.setPath(path);

                rootsCount++;
                /*insertPostWithTimestamp(objPost, holder, now);

                objPost.setId((int) holder.getKeys().get("id"));*/

            } else {
                final String prevPath = jdbcTemplate.queryForObject(
                        "SELECT path FROM post WHERE id = ?;",
                        new Object[]{objPost.getParent()}, String.class
                );

                objPost.setPath('*'+prevPath);
                /*insertPostWithTimestamp(objPost, holder, now);

                final int id = (int) holder.getKeys().get("id");
                objPost.setId(id);*/

                /*final String path = prevPath + '.' + ValueConverter.toHex(id);
                objPost.setPath(path);
                jdbcTemplate.update(
                        "UPDATE post SET path=? WHERE id=?",
                        new Object[]{path, id});*/
            }

            objPost.setCreated(now);

            //result.put(objPost.getJson());

            postList.add(new Object[]{
                    objPost.getParent(),
                    objPost.getAuthor(),
                    objPost.getMessage(),
                    objPost.getEdited(),
                    objPost.getForum(),
                    objPost.getThread(),
                    objPost.getPath(),
                    objPost.getCreated()
            });
        }

        //List<Integer> idsList = insertAndReturnIds(postList);
        final int maxId = getMaxId();
        jdbcTemplate.batchUpdate("INSERT INTO post (parent,author,message,isEdited,forum,thread,path,created) " +
                "VALUES (?,?,?,?,?,?,?,?::timestamp with time zone)", postList);
        List<Integer> idsList = getPostIdsAfterId(maxId);

        getThreadsAfterId(maxId, threadID);

        IntStream.range(0, arrObjPost.size()).boxed()
                .forEach(i -> {
                    arrObjPost.get(i).setId(idsList.get(i));
                    result.put(arrObjPost.get(i).getJson());
                });

        jdbcTemplate.update(
                "UPDATE forum SET posts=posts+" + arrObjPost.size() + " WHERE LOWER(slug)=LOWER(?)",
                objThread.getForum());

        return new ResponseEntity<>(result.toString(), HttpStatus.CREATED);
    }

    public void getThreadsAfterId(int postId, int threadId){
        List<Integer> threadList = jdbcTemplate.queryForList("SELECT thread FROM post WHERE id > ? ORDER BY id", new Object[]{postId}, Integer.class);
        threadList = threadList.stream().distinct().collect(Collectors.toList());
        StringBuilder list = new StringBuilder("THREAD=" + threadId + "; arr=");
        for(Integer id : threadList){
            list.append(id).append(", ");
        }
        System.out.println(list);
    }


    /*@Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<Integer> insertAndReturnIds(List<Object[]> postList){
        final int maxId = getMaxId();
        jdbcTemplate.batchUpdate("INSERT INTO post (parent,author,message,isEdited,forum,thread,path,created) " +
                "VALUES (?,?,?,?,?,?,?,?::timestamp with time zone)", postList);
        return getPostIdsAfterId(maxId);

    }*/

    /*public void insertPostWithTimestamp(ObjPost objPost, KeyHolder holder, Timestamp time) {
        jdbcTemplate.update(connection -> {
            final PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO post (parent,author,message,isEdited,forum,thread,path,created) " +
                            "VALUES (?,?,?,?,?,?,?,?::timestamp with time zone)", new String[]{"id", "created"});
            ps.setInt(1, objPost.getParent());
            ps.setString(2, objPost.getAuthor());
            ps.setString(3, objPost.getMessage());
            ps.setBoolean(4, objPost.getEdited());
            ps.setString(5, objPost.getForum());
            ps.setInt(6, objPost.getThread());
            ps.setString(7, objPost.getPath());
            ps.setTimestamp(8, time);
            return ps;
        }, holder);
    }*/

    public ResponseEntity<String> vote(ObjVote objVote, String slug_or_id) {
        final ObjThread result;

        if (new UserService(jdbcTemplate).getObjUser(objVote.getNickname()) == null) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }

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
                "SELECT * FROM vote WHERE(id, LOWER(nickname))=(?,LOWER(?))",
                new Object[]{threadId, objVote.getNickname()}, new VoteMapper());

        String sql = "";
        if (!objVoteList.isEmpty() && objVoteList.get(0).getVoice() == -1) {
            sql = "UPDATE thread SET votes = votes + 2 WHERE id = ?; " +
                    "UPDATE vote SET voice = 1 WHERE id = ? AND nickname = ?";
        } else if(objVoteList.isEmpty()){
            sql = "UPDATE thread SET votes = votes + 1 WHERE id = ?; " +
                    "INSERT INTO vote (id, nickname, voice) VALUES (?, ?, 1)";
        }
        if(!sql.equals(""))
            voteDeadlock(sql, threadId, objVote.getNickname());
    }

    public void unvote(ObjVote objVote, Integer threadId) throws DataAccessException {
        final List<ObjVote> objVoteList = jdbcTemplate.query(
                "SELECT * FROM vote WHERE(id, LOWER(nickname))=(?,LOWER(?))",
                new Object[]{threadId, objVote.getNickname()}, new VoteMapper());
            String sql ="";
            if (!objVoteList.isEmpty() && objVoteList.get(0).getVoice() == 1) {
                sql = "UPDATE thread SET votes = votes - 2 WHERE id = ?; " +
                        "UPDATE vote SET voice = -1 WHERE id = ? AND nickname = ?";
            } else if(objVoteList.isEmpty()){
                sql = "UPDATE thread SET votes = votes - 1 WHERE id = ?; " +
                        "INSERT INTO vote (id, nickname, voice) VALUES (?, ?, -1)";
            }
            if(!sql.equals(""))
                voteDeadlock(sql, threadId, objVote.getNickname());
    }

    private void voteDeadlock(String sql, int threadId, String nickname) {
        try {
            boolean finished = false;
            while (!finished) {
                jdbcTemplate.update(sql, threadId, threadId, nickname);
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
                    System.out.println("query=" + postQuery);
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
                    System.out.println("query=" + postQuery);
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
                    System.out.println("query=" + postQuery);
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
