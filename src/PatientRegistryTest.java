import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PatientRegistryTest {
    private PatientRegistry registry;

    @BeforeEach
    void setup() {
        registry = new PatientRegistry();
    }

    @Test
    void registerAndFindPatient() {
        Patient p = new Patient(1, "Alice", "Zephyr", 30, "F", "Flu", PatientCategory.OUTPATIENT);
        registry.registerPatient(p);
        assertTrue(registry.findById(1).isPresent());
    }

    @Test
    void preventDuplicateIds() {
        Patient p1 = new Patient(2, "Bob", "Alpha", 40, "M", "Cold", PatientCategory.OUTPATIENT);
        registry.registerPatient(p1);
        Patient p2 = new Patient(2, "Bobby", "Beta", 25, "M", "Injury", PatientCategory.EMERGENCY);
        assertThrows(IllegalArgumentException.class, () -> registry.registerPatient(p2));
    }

    @Test
    void updateAndDeletePatient() {
        Patient p = new Patient(3, "Cara", "Delta", 28, "F", "Check", PatientCategory.OUTPATIENT);
        registry.registerPatient(p);
        Patient updated = new Patient(3, "Cara", "DeltaX", 29, "F", "Checkup", PatientCategory.OUTPATIENT);
        assertTrue(registry.updatePatient(3, updated));
        assertEquals("DeltaX", registry.findById(3).get().getLastName());
        assertTrue(registry.deletePatient(3));
        assertFalse(registry.findById(3).isPresent());
    }

    @Test
    void sortPatientsBySurnameAndId() {
        registry.registerPatient(new Patient(5, "A", "Zulu", 20, "F", "", PatientCategory.OUTPATIENT));
        registry.registerPatient(new Patient(1, "B", "Alpha", 30, "M", "", PatientCategory.OUTPATIENT));
        registry.registerPatient(new Patient(3, "C", "Beta", 40, "M", "", PatientCategory.OUTPATIENT));
        var bySurname = registry.sortedBySurname();
        assertEquals("Alpha", bySurname.get(0).getLastName());
        var byId = registry.sortedById();
        assertEquals(1, byId.get(0).getId());
    }
}