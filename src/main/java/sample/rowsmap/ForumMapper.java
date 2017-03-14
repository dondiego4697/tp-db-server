package sample.rowsmap;

import org.springframework.jdbc.core.RowMapper;
import sample.objects.ObjForum;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Denis on 14.03.2017.
 */
public class ForumMapper implements RowMapper<ObjForum> {

    @Override
    public ObjForum mapRow(ResultSet resultSet, int i) throws SQLException {
        final ObjForum objForum = new ObjForum();
        objForum.setTitle(resultSet.getString("title"));
        objForum.setUser(resultSet.getString("user"));
        objForum.setSlug(resultSet.getString("slug"));
        objForum.setPosts(resultSet.getInt("posts"));
        objForum.setThreads(resultSet.getInt("threads"));
        return objForum;
    }
}
