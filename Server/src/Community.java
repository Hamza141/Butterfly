import java.util.Date;

public class Community {
    private int id;
    private int categoryId;
    private int neighborhoodId;

    private int numMembers;
    private int numUpcomingEvents;

    private String name;
    private String description;
    private String[] subcategories;

    private Date dateCreated;

    public int getId() {
        return this.id;
    }

    public int getCategoryId() {
        return this.categoryId;
    }

    public int getNeighborhoodId() {
        return this.neighborhoodId;
    }

    public int getNumMembers() {
        return this.neighborhoodId;
    }

    public int getNumUpcomingEvents() {
        return this.numUpcomingEvents;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String[] getSubcategories() {
        return this.subcategories;
    }

}
