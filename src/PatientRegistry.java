import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PatientRegistry {
    private final List<Patient> patients = new ArrayList<>();

    public void registerPatient(Patient p) {
        if (findById(p.getId()).isPresent()) {
            throw new IllegalArgumentException("Patient ID " + p.getId() + " already exists.");
        }
        patients.add(p);
    }

    public Optional<Patient> findById(int id) {
        return patients.stream().filter(pt -> pt.getId() == id).findFirst();
    }

    public boolean updatePatient(int id, Patient updated) {
        Optional<Patient> opt = findById(id);
        if (opt.isEmpty()) return false;
        Patient existing = opt.get();
        // update fields (except class type); if switching category to INPATIENT, caller should create Inpatient
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setAge(updated.getAge());
        existing.setGender(updated.getGender());
        existing.setMedicalCondition(updated.getMedicalCondition());
        existing.setCategory(updated.getCategory());
        // ID change: ensure no duplicate
        if (updated.getId() != id) {
            if (findById(updated.getId()).isPresent()) {
                throw new IllegalArgumentException("Target ID " + updated.getId() + " already exists.");
            }
            existing.setId(updated.getId());
        }
        return true;
    }

    public boolean deletePatient(int id) {
        Optional<Patient> opt = findById(id);
        if (opt.isPresent()) {
            patients.remove(opt.get());
            return true;
        }
        return false;
    }

    public List<Patient> listAll() {
        return new ArrayList<>(patients);
    }

    public int count() {
        return patients.size();
    }

    public List<Patient> sortedBySurname() {
        List<Patient> copy = new ArrayList<>(patients);
        copy.sort(Comparator.comparing(Patient::getLastName).thenComparing(Patient::getFirstName));
        return copy;
    }

    public List<Patient> sortedById() {
        List<Patient> copy = new ArrayList<>(patients);
        copy.sort(Comparator.comparingInt(Patient::getId));
        return copy;
    }
}