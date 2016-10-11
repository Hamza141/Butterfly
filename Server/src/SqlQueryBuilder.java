/**
 * Class to build sql queries from Requests
 */

public class SqlQueryBuilder {
    private Request request;
    private String sql;

    public SqlQueryBuilder(Request request) {
        this.request = request;
    }

    public void createQuery() {
        RequestTypes requestType = request.getRequestType();
        switch (requestType) {
            case CREATE:
                sql = buildCreateQuery();
                break;
            case REMOVE:
                sql = buildDeleteQuery();
                break;
            case MODIFY:
                sql = buildModifyQuery();
                break;
            case ADD:
                sql = buildAddQuery();
                break;
            case READ:
                sql = buildReadQuery();
                break;
        }
    }

    public String getSql() {
        return sql;
    }

    private String buildCreateQuery() {
        InsertQuery query = new InsertQuery();
        query.setTableName(request.getPrimaryObjType().getTableName());
        query.setColumns(request.getPrimaryColumns());
        query.setValues(request.getPrimaryValues());
        query.makeQuery();
        return query.getQuery();
    }

    private String buildDeleteQuery() {
        DeleteQuery query = new DeleteQuery();
        query.setTableName(request.getPrimaryObjType().getTableName());
        query.setColumns(request.getPrimaryColumns());
        query.setValues(request.getPrimaryValues());
        query.makeQuery();
        return query.getQuery();
    }

    private String buildModifyQuery() {
        UpdateQuery query = new UpdateQuery();
        query.setTableName(request.getPrimaryObjType().getTableName());
        query.setColumns(request.getPrimaryColumns());
        query.setValues(request.getPrimaryValues());
        query.setWhereCol("id");
        query.setWhereVal(request.getPrimaryValueFor("id"));
        query.makeQuery();
        return query.getQuery();
    }

    private String buildAddQuery() {
        InsertQuery query = new InsertQuery();
        query.setTableName(request.getPrimaryObjType().getTableName());
        query.setColumns(request.getPrimaryColumns());
        query.setValues(request.getPrimaryValues());
        query.makeQuery();
        return query.getQuery();
    }

    private String buildReadQuery() {
        SelectQuery query = new SelectQuery();
        query.setTableName(request.getPrimaryObjType().getTableName());
        query.setColumns(request.getPrimaryColumns());
        query.makeQuery();
        return query.getQuery();
    }
}
