public class Inpatient extends Patient {
    private int wardNumber;
    private String bedId;

    public Inpatient(int id, String firstName, String lastName, int age, String gender, String medicalCondition, int wardNumber, String bedId) {
        super(id, firstName, lastName, age, gender, medicalCondition, PatientCategory.INPATIENT);
        this.wardNumber = wardNumber;
        this.bedId = bedId;
    }

    public Inpatient(int id, String firstName, String lastName, int age, String gender, String medicalCondition) {
        this(id, firstName, lastName, age, gender, medicalCondition, -1, null);
    }

    public int getWardNumber() { return wardNumber; }
    public void setWardNumber(int wardNumber) { this.wardNumber = wardNumber; }

    public String getBedId() { return bedId; }
    public void setBedId(String bedId) { this.bedId = bedId; }

    @Override
    public String displayDetails() {
        String base = super.displayDetails();
        String wardInfo = (bedId != null ? String.format(", ward=%d, bed=%s", wardNumber, bedId) : ", ward=unassigned, bed=unassigned");
        return base.replaceFirst("\\}$", "") + wardInfo + "}";
    }
}