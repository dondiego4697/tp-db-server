package sample.rowsmap;

import org.springframework.jdbc.core.RowMapper;
import sample.objects.ObjVote;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Denis on 20.03.2017.
 */
public class VoteMapper implements RowMapper<ObjVote> {
    @Override
    public ObjVote mapRow(ResultSet resultSet, int i) throws SQLException {
        final ObjVote objVote = new ObjVote();
        objVote.setThreadId(resultSet.getInt("id"));
        objVote.setUserid(resultSet.getInt("userid"));
        objVote.setSlug(resultSet.getString("slug"));
        objVote.setVoice(resultSet.getInt("voice"));
        return objVote;
    }
}
