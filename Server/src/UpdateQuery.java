import java.util.ArrayList;
import java.util.Iterator;

public class UpdateQuery extends CustomSqlQuery {
    String whereCol;
    String whereVal;

    public UpdateQuery() {
        columns = new ArrayList<String>();
        values = new ArrayList<String>();
    }

    public void setWhereCol(String whereCol) {
        this.whereCol = whereCol;
    }

    public void setWhereVal(String whereVal) {
        this.whereVal = whereVal;
    }

    public void makeQuery() {
        String setStr = createSetStr();
        String whereStr = createWhereStr();
        query = "UPDATE "  +
                tableName  +
                " SET "    +
                setStr     +
                " WHERE "  +
                whereStr;
    }

    private String createSetStr() {
        Iterator<String> columnsItr = columns.iterator();
        Iterator<String> valuesItr = values.iterator();
        String str = "";
        while (columnsItr.hasNext()) {
            str += columnsItr.next() + "=" + valuesItr.next();
            if (columnsItr.hasNext()) {
                str += ", ";
            }
        }
        return str;
    }

    private String createWhereStr() {
        return whereCol + "=" + whereVal;
    }

}
