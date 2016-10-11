import java.util.ArrayList;

/**
 * Created by nick on 10/10/16.
 */
public class DeleteQuery extends CustomSqlQuery{

    public DeleteQuery() {
        columns = new ArrayList<String>();
        values = new ArrayList<String>();
    }

    public void makeQuery() {
        String whereStr = createWhereStr();
        query = "DELETE FROM "  +
                tableName       +
                "WHERE "        +
                whereStr;
    }

    protected String createWhereStr() {
        return columns.get(0) + "=" + values.get(0);
    }
}
