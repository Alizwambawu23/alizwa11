import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BedManagement {
    private final Bed[][] beds;
    private final int rows;
    private final int cols;
    private final int wardNumber = 1; // single ward per assumptions

    public BedManagement(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.beds = new Bed[rows][cols];
        initBeds();
    }

    private void initBeds() {
        int id = 1;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                beds[r][c] = new Bed(String.format("B%02d", id++));
            }
        }
    }

    // Allocate to a patient by id; patient must exist and be an Inpatient type
    public String allocateBed(int patientId, PatientRegistry registry) {
        Optional<Patient> opt = registry.findById(patientId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Patient " + patientId + " not found.");
        Patient p = opt.get();
        if (!(p instanceof Inpatient)) throw new IllegalArgumentException("Only inpatients may be allocated a bed.");
        Inpatient ip = (Inpatient)p;
        // if already has bed, prevent double allocation
        if (ip.getBedId() != null) throw new IllegalStateException("Patient already allocated bed " + ip.getBedId());
        int[] pos = findFirstAvailableBed();
        if (pos == null) throw new IllegalStateException("No beds available.");
        Bed bed = beds[pos[0]][pos[1]];
        bed.allocate(patientId, ip.getFirstName() + " " + ip.getLastName());
        ip.setWardNumber(wardNumber);
        ip.setBedId(bed.getId());
        return bed.getId();
    }

    public boolean releaseBed(String bedId, PatientRegistry registry) {
        int[] pos = findBedById(bedId);
        if (pos == null) return false;
        Bed bed = beds[pos[0]][pos[1]];
        if (!bed.isOccupied()) return false;
        Integer pid = bed.getPatientId();
        bed.release();
        // if patient still in registry and is Inpatient, clear bed/ward info
        if (pid != null) {
            Optional<Patient> opt = registry.findById(pid);
            if (opt.isPresent() && opt.get() instanceof Inpatient) {
                Inpatient ip = (Inpatient) opt.get();
                ip.setBedId(null);
                ip.setWardNumber(-1);
            }
        }
        return true;
    }

    public List<Bed> availableBeds() {
        List<Bed> list = new ArrayList<>();
        for (Bed[] row : beds) for (Bed b : row) if (!b.isOccupied()) list.add(b);
        return list;
    }

    public List<Bed> occupiedBeds() {
        List<Bed> list = new ArrayList<>();
        for (Bed[] row : beds) for (Bed b : row) if (b.isOccupied()) list.add(b);
        return list;
    }

    public List<Bed> allBeds() {
        List<Bed> list = new ArrayList<>();
        for (Bed[] row : beds) for (Bed b : row) list.add(b);
        return list;
    }

    public int countOccupied() {
        return occupiedBeds().size();
    }

    public double occupancyPercentage() {
        int total = rows * cols;
        return total == 0 ? 0.0 : (countOccupied() * 100.0 / total);
    }

    // Helpers
    private int[] findFirstAvailableBed() {
        for (int r = 0; r < beds.length; r++) {
            for (int c = 0; c < beds[r].length; c++) {
                if (!beds[r][c].isOccupied()) return new int[]{r, c};
            }
        }
        return null;
    }

    private int[] findBedById(String id) {
        for (int r = 0; r < beds.length; r++) {
            for (int c = 0; c < beds[r].length; c++) {
                if (beds[r][c].getId().equalsIgnoreCase(id)) return new int[]{r, c};
            }
        }
        return null;
    }
}