package sample.sql;

import com.sun.istack.internal.NotNull;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import sample.objects.ObjUser;
import sample.rowsmap.UserMapper;

import java.util.List;

/**
 * Created by Denis on 15.03.2017.
 */
public class UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public UserService(@NotNull JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ResponseEntity<String> create(ObjUser objUser, String nickname) {
        objUser.setNickname(nickname);
        final List<ObjUser> users =
                jdbcTemplate.query("SELECT * FROM users WHERE LOWER (email)=? or LOWER (nickname)=?",
                        new Object[]{objUser.getEmail().toLowerCase(),
                                objUser.getNickname().toLowerCase()}, new UserMapper());
        if (users.isEmpty()) {
            jdbcTemplate.update("INSERT INTO users (nickname,fullname,about,email) VALUES (?,?,?,?)",
                    objUser.getNickname(), objUser.getFullname(),
                    objUser.getAbout(), objUser.getEmail());
            return new ResponseEntity<>(objUser.getJson().toString(), HttpStatus.CREATED);
        }
        final JSONArray result = new JSONArray();
        for (ObjUser user : users) {
            result.put(user.getJson());
        }
        return new ResponseEntity<>(result.toString(), HttpStatus.CONFLICT);
    }
}
