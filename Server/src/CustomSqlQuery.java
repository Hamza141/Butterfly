/**
 * Abstract class for different the different database interactions
 * Implemented by: DeleteQuery
 *                 InsertQuery
 *                 SelectQuery
 *                 UpdateQuery
 */

import java.util.Iterator;
import java.util.List;

public abstract class CustomSqlQuery {
    protected String query;
    protected String tableName;
    protected List<String> columns;
    protected List<String> values;

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void addColumn(String column) {
        this.columns.add(column);
    }

    public void addValue(String value) {
        this.columns.add(value);
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    protected String listElements(List<String> elements) {
        String elementsStr = "";
        Iterator<String> elementsItr = elements.iterator();
        while (elementsItr.hasNext()) {
            elementsStr += elementsItr.next();
            if (elementsItr.hasNext()) {
                elementsStr += ", ";
            }
        }
        return elementsStr;
    }

    public String getQuery() {
        return query;
    }

    public abstract void makeQuery();
}
