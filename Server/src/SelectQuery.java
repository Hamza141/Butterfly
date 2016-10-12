import java.util.ArrayList;

public class SelectQuery extends CustomSqlQuery {

    public SelectQuery() {
        columns = new ArrayList<String>();
    }

    public void makeQuery() {
        String columnsStr = listElements(columns);
        query = "SELECT "   +
                columnsStr  +
                "FROM "     +
                tableName;
    }
}
