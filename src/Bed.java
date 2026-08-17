public class Bed {
    private final String id; // B01..B20
    private boolean occupied;
    private Integer patientId; // null if free
    private String patientName;

    public Bed(String id) {
        this.id = id;
        this.occupied = false;
    }

    public String getId() { return id; }
    public boolean isOccupied() { return occupied; }
    public Integer getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }

    public void allocate(int patientId, String patientName) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.occupied = true;
    }

    public void release() {
        this.patientId = null;
        this.patientName = null;
        this.occupied = false;
    }

    @Override
    public String toString() {
        if (occupied) return id + " - OCCUPIED by [" + patientId + "] " + patientName;
        return id + " - AVAILABLE";
    }
}
