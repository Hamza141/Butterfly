import java.util.ArrayList;

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
