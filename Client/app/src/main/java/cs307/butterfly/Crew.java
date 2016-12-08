package cs307.butterfly;

public class Crew {
    private String crewName;
    private int idNumber;

    public Crew(String crewName, int idNumber) {
        this.crewName = crewName;
        this.idNumber = idNumber;
    }

    public String getCrewName() {
        return this.crewName;
    }

    public int getIdNumber() {
        return this.idNumber;
    }
}
