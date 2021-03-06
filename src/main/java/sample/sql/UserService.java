package sample.sql;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import sample.objects.ObjUser;
import sample.rowsmap.UserMapper;

import java.util.List;

/**
 * Created by Denis on 15.03.2017.
 */
@Service
public class UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /*public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }*/

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ResponseEntity<String> create(ObjUser objUser, String nickname) throws DataAccessException {
        objUser.setNickname(nickname);
        jdbcTemplate.update("INSERT INTO users (nickname,fullname,about,email) VALUES (?,?,?,?)",
                objUser.getNickname(), objUser.getFullname(),
                objUser.getAbout(), objUser.getEmail());

        return new ResponseEntity<>(objUser.getJson().toString(), HttpStatus.CREATED);
    }

    public String getUsers(ObjUser objUser){
        final List<ObjUser> users = jdbcTemplate.query(
                "SELECT * FROM users WHERE LOWER(email)=LOWER(?) OR LOWER(nickname)=LOWER(?)",
                new Object[]{objUser.getEmail(),
                        objUser.getNickname()}, new UserMapper());
        final JSONArray result = new JSONArray();

        for (ObjUser user : users) {
            result.put(user.getJson());
        }
        return result.toString();
    }

    public ObjUser getObjUser(String nickname){
        try {
            final ObjUser user = jdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?)",
                    new Object[]{nickname}, new UserMapper());
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    public ResponseEntity<String> get(String nickname) {
        try {
            final ObjUser user = jdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE LOWER(nickname) = LOWER(?)",
                    new Object[]{nickname}, new UserMapper());
            return new ResponseEntity<>(user.getJson().toString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<String> update(ObjUser objUser, String nickname) {
        if (objUser.getJson().toString().equals("{}")) return get(nickname);

        final ResponseEntity<String> resultGet = get(nickname);
        if (resultGet.getStatusCode() == HttpStatus.NOT_FOUND) return resultGet;

        final JSONObject prevUser = new JSONObject(resultGet.getBody());
        final JSONObject newUser = objUser.getJson();

        if (!newUser.has("about")) objUser.setAbout(prevUser.get("about").toString());
        if (!newUser.has("email")) objUser.setEmail(prevUser.get("email").toString());
        if (!newUser.has("fullname")) objUser.setFullname(prevUser.get("fullname").toString());
        objUser.setNickname(nickname);

        try {
            jdbcTemplate.update(
                    "UPDATE users SET (fullname,about,email)=(?,?,?)" +
                            " WHERE LOWER(nickname)= (?)",
                    objUser.getFullname(),
                    objUser.getAbout(),
                    objUser.getEmail(),
                    nickname.toLowerCase());
            return new ResponseEntity<>(objUser.getJson().toString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.CONFLICT);
        }
    }
}
