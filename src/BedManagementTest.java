import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BedManagementTest {
    private BedManagement bedMgmt;
    private PatientRegistry registry;

    @BeforeEach
    void setup() {
        bedMgmt = new BedManagement(4, 5);
        registry = new PatientRegistry();
    }

    @Test
    void allocateAndReleaseBed() {
        Inpatient ip = new Inpatient(10, "Dana", "X", 50, "F", "Surgery");
        registry.registerPatient(ip);
        String bedId = bedMgmt.allocateBed(10, registry);
        assertNotNull(bedId);
        assertEquals(1, bedMgmt.countOccupied());
        assertTrue(bedMgmt.releaseBed(bedId, registry));
        assertEquals(0, bedMgmt.countOccupied());
        // patient bed info cleared
        assertNull(((Inpatient) registry.findById(10).get()).getBedId());
    }

    @Test
    void preventAllocatingOccupiedBedAndWhenFull() {
        // Create and allocate 20 inpatients
        for (int i = 1; i <= 20; i++) {
            Inpatient ip = new Inpatient(100 + i, "P" + i, "Surname", 30, "M", "Cond");
            registry.registerPatient(ip);
            String bedId = bedMgmt.allocateBed(100 + i, registry);
            assertNotNull(bedId);
        }
        assertEquals(20, bedMgmt.countOccupied());
        // Now no beds available: next allocation throws
        Inpatient extra = new Inpatient(999, "Extra", "E", 45, "F", "Cond");
        registry.registerPatient(extra);
        assertThrows(IllegalStateException.class, () -> bedMgmt.allocateBed(999, registry));
    }

    @Test
    void preventAllocatingToNonInpatient() {
        Patient p = new Patient(200, "Oliver", "Q", 34, "M", "Check", PatientCategory.OUTPATIENT);
        registry.registerPatient(p);
        assertThrows(IllegalArgumentException.class, () -> bedMgmt.allocateBed(200, registry));
    }
}