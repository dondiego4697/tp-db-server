package sample.rowsmap;

import org.springframework.jdbc.core.RowMapper;
import sample.objects.ObjThread;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Denis on 14.03.2017.
 */
public class ThreadMapper implements RowMapper<ObjThread> {

    @Override
    public ObjThread mapRow(ResultSet resultSet, int i) throws SQLException {
        final ObjThread objThread = new ObjThread();
        objThread.setId(resultSet.getInt("id"));
        objThread.setTitle(resultSet.getString("title"));
        objThread.setAuthor(resultSet.getString("author"));
        objThread.setForum(resultSet.getString("forum"));
        objThread.setMessage(resultSet.getString("message"));
        objThread.setUserid(resultSet.getInt("userid"));
        if (resultSet.getInt("votes") != 0) objThread.setVotes(resultSet.getInt("votes"));
        objThread.setSlug(resultSet.getString("slug"));
        objThread.setCreated(resultSet.getString("created"));
        return objThread;
    }
}
