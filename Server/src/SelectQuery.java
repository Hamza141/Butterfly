import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by nick on 10/10/16.
 */
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
