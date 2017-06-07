package sample.rowsmap;

import org.springframework.jdbc.core.RowMapper;
import sample.objects.ObjUser;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Denis on 14.03.2017.
 */
public class UserMapper implements RowMapper<ObjUser> {
    @Override
    public ObjUser mapRow(ResultSet resultSet, int i) throws SQLException {
        final ObjUser objUser = new ObjUser();
        objUser.setId(resultSet.getInt("id"));
        objUser.setNickname(resultSet.getString("nickname"));
        objUser.setFullname(resultSet.getString("fullname"));
        objUser.setAbout(resultSet.getString("about"));
        objUser.setEmail(resultSet.getString("email"));
        return objUser;
    }
}
