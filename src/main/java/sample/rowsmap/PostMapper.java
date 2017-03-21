package sample.rowsmap;

import org.springframework.jdbc.core.RowMapper;
import sample.objects.ObjPost;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Denis on 20.03.2017.
 */
public class PostMapper implements RowMapper<ObjPost> {
    @Override
    public ObjPost mapRow(ResultSet resultSet, int i) throws SQLException {
        final ObjPost objPost = new ObjPost();
        objPost.setId(resultSet.getInt("id"));
        objPost.setAuthor(resultSet.getString("author"));
        objPost.setCreated(resultSet.getString("created"));
        objPost.setForum(resultSet.getString("forum"));
        objPost.setEdited(resultSet.getBoolean("isEdited"));
        objPost.setMessage(resultSet.getString("message"));
        objPost.setParent(resultSet.getInt("parent"));
        objPost.setThread(resultSet.getInt("thread"));
        objPost.setPath(resultSet.getString("path"));
        return objPost;
    }
}
