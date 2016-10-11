import java.util.ArrayList;

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

    private String createWhereStr() {
        return columns.get(0) + "=" + values.get(0);
    }
}
