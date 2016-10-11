import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by nick on 10/10/16.
 */
public class InsertQuery extends CustomSqlQuery {

    public InsertQuery() {
        columns = new ArrayList<String>();
        values = new ArrayList<String>();
    }

    public void makeQuery() {
        String columnsStr = listElements(columns);
        String valuesStr = listElements(values);
        query = "INSERT INTO "  +
                tableName       +
                " ("            +
                columnsStr      +
                ") "            +
                "VALUES ("      +
                valuesStr       +
                ")";
    }
}
