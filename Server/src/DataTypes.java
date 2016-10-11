import java.util.ArrayList;
import java.util.List;

/**
 * Created by nick on 10/9/16.
 */
public enum DataTypes {
    COMMUNITY,
    USER,
    EVENT;

    public static List<String> getColumnNames(DataTypes type) {
        List<String> columns = new ArrayList<String>();
        switch (type) {
            case COMMUNITY:
                columns.add("id");
                columns.add("neighborhoodId");
                columns.add("categoryId");
                columns.add("name");
                columns.add("description");
                columns.add("numMembers");
                columns.add("numUpcomingEvents");
                columns.add("dateCreated");
                break;
            default:
                break;
        }
        return columns;
    }

    public String getTableName() {
        String tableName;
        switch (this) {
            case COMMUNITY:
                tableName = "Communities";
                break;
            case USER:
                tableName = "Users";
                break;
            case EVENT:
                tableName = "Events";
                break;
            default:
                tableName = null;
        }
        return tableName;
    }
}
