import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private final PatientRegistry registry = new PatientRegistry();
    private final BedManagement bedMgmt = new BedManagement(4, 5);
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        while (true) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": patientMenu(); break;
                case "2": bedMenu(); break;
                case "3": reportsMenu(); break;
                case "4": System.out.println("Exiting. Goodbye."); return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private void printMainMenu() {
        System.out.println("---- Hospital System---- ");
        System.out.println("1. Patient Management");
        System.out.println("2. Bed Management");
        System.out.println("3. Reports");
        System.out.println("4. Exit");
        System.out.print("Choose: ");
    }

    // Patient menu
    private void patientMenu() {
        while (true) {
            System.out.println("-- Patient Management --");
            System.out.println("1. Register new patient");
            System.out.println("2. Search patient by ID");
            System.out.println("3. Update existing patient");
            System.out.println("4. Delete patient");
            System.out.println("5. Display all patients");
            System.out.println("6. Sort patients (by surname)");
            System.out.println("7. Sort patients (by ID)");
            System.out.println("8. Back");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1": registerPatientConsole(); break;
                    case "2": searchPatientConsole(); break;
                    case "3": updatePatientConsole(); break;
                    case "4": deletePatientConsole(); break;
                    case "5": displayAllPatientsConsole(); break;
                    case "6": displayList(registry.sortedBySurname()); break;
                    case "7": displayList(registry.sortedById()); break;
                    case "8": return;
                    default: System.out.println("Invalid option.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private void registerPatientConsole() {
        System.out.println("-- Register patient --");
        int id = readInt("ID: ");
        if (registry.findById(id).isPresent()) {
            System.out.println("Duplicate ID. Registration cancelled.");
            return;
        }
        String fn = readString("First name: ");
        String ln = readString("Last name: ");
        int age = readInt("Age: ");
        String gender = readString("Gender: ");
        String cond = readString("Medical condition: ");
        System.out.print("Patient category (inpatient/outpatient/emergency): ");
        String cat = scanner.nextLine().trim().toLowerCase();
        PatientCategory category;
        switch (cat) {
            case "inpatient": category = PatientCategory.INPATIENT; break;
            case "outpatient": category = PatientCategory.OUTPATIENT; break;
            case "emergency": category = PatientCategory.EMERGENCY; break;
            default:
                System.out.println("Unknown category. Defaulting to OUTPATIENT.");
                category = PatientCategory.OUTPATIENT;
        }
        Patient p;
        if (category == PatientCategory.INPATIENT) {
            p = new Inpatient(id, fn, ln, age, gender, cond);
        } else {
            p = new Patient(id, fn, ln, age, gender, cond, category);
        }
        registry.registerPatient(p);
        System.out.println("Registered: " + p.displayDetails());
    }

    private void searchPatientConsole() {
        int id = readInt("Enter ID to search: ");
        Optional<Patient> opt = registry.findById(id);
        if (opt.isEmpty()) System.out.println("No patient with ID " + id);
        else System.out.println(opt.get().displayDetails());
    }

    private void updatePatientConsole() {
        int id = readInt("Enter ID to update: ");
        Optional<Patient> opt = registry.findById(id);
        if (opt.isEmpty()) { System.out.println("Not found."); return; }
        Patient existing = opt.get();
        System.out.println("Current: " + existing.displayDetails());
        System.out.println("Press Enter to keep current value.");
        String newIdStr = readStringAllowEmpty("New ID (current: " + existing.getId() + "): ");
        int newId = existing.getId();
        if (!newIdStr.isEmpty()) newId = Integer.parseInt(newIdStr);
        String fn = readStringAllowEmpty("First name (current: " + existing.getFirstName() + "): ");
        if (fn.isEmpty()) fn = existing.getFirstName();
        String ln = readStringAllowEmpty("Last name (current: " + existing.getLastName() + "): ");
        if (ln.isEmpty()) ln = existing.getLastName();
        String ageStr = readStringAllowEmpty("Age (current: " + existing.getAge() + "): ");
        int age = existing.getAge();
        if (!ageStr.isEmpty()) age = Integer.parseInt(ageStr);
        String gender = readStringAllowEmpty("Gender (current: " + existing.getGender() + "): ");
        if (gender.isEmpty()) gender = existing.getGender();
        String cond = readStringAllowEmpty("Medical condition (current: " + existing.getMedicalCondition() + "): ");
        if (cond.isEmpty()) cond = existing.getMedicalCondition();
        System.out.print("Patient category (inpatient/outpatient/emergency) (current: " + existing.getCategory() + "): ");
        String cat = scanner.nextLine().trim();
        PatientCategory category = existing.getCategory();
        if (!cat.isEmpty()) {
            switch (cat.toLowerCase()) {
                case "inpatient": category = PatientCategory.INPATIENT; break;
                case "outpatient": category = PatientCategory.OUTPATIENT; break;
                case "emergency": category = PatientCategory.EMERGENCY; break;
                default: System.out.println("Unknown input; keeping current category.");
            }
        }

        // If changing between types, handle Inpatient conversion
        Patient updated;
        if (category == PatientCategory.INPATIENT) {
            if (existing instanceof Inpatient) {
                updated = existing; // will update fields below
                updated.setFirstName(fn); updated.setLastName(ln); updated.setAge(age);
                updated.setGender(gender); updated.setMedicalCondition(cond); updated.setCategory(category);
                if (newId != existing.getId()) updated.setId(newId);
            } else {
                // convert to Inpatient (bed unassigned until allocation)
                updated = new Inpatient(newId, fn, ln, age, gender, cond);
                // remove old and register new: easier to delete then register
                registry.deletePatient(existing.getId());
                registry.registerPatient(updated);
                System.out.println("Converted to Inpatient (no bed assigned yet).");
                return;
            }
        } else {
            // Non-inpatient -> base Patient
            if (existing instanceof Inpatient) {
                // converting inpatient to outpatient/emergency: release bed if any
                Inpatient ip = (Inpatient) existing;
                if (ip.getBedId() != null) {
                    System.out.println("Patient was inpatient with bed " + ip.getBedId() + ". Please release bed first before changing category.");
                    return;
                }
            }
            updated = new Patient(newId, fn, ln, age, gender, cond, category);
            // replace in registry
            registry.deletePatient(existing.getId());
            registry.registerPatient(updated);
            System.out.println("Updated patient.");
            return;
        }
        // If code reaches here, updated is same object for inpatient update
        System.out.println("Updated: " + updated.displayDetails());
    }

    private void deletePatientConsole() {
        int id = readInt("Enter ID to delete: ");
        Optional<Patient> opt = registry.findById(id);
        if (opt.isEmpty()) { System.out.println("Not found."); return; }
        Patient p = opt.get();
        if (p instanceof Inpatient) {
            Inpatient ip = (Inpatient)p;
            if (ip.getBedId() != null) {
                System.out.println("Patient occupies bed " + ip.getBedId() + ". Release bed before deleting.");
                return;
            }
        }
        registry.deletePatient(id);
        System.out.println("Deleted patient " + id);
    }

    private void displayAllPatientsConsole() {
        List<Patient> all = registry.listAll();
        if (all.isEmpty()) System.out.println("No patients registered.");
        else displayList(all);
    }

    private void displayList(List<Patient> list) {
        for (Patient p : list) System.out.println(p.displayDetails());
    }

    // Bed menu
    private void bedMenu() {
        while (true) {
            System.out.println("--- Bed Management ---");
            System.out.println("1. Allocate an available bed to an inpatient");
            System.out.println("2. Release a bed");
            System.out.println("3. Display occupied beds");
            System.out.println("4. Display available beds");
            System.out.println("5. Display all beds (grid)");
            System.out.println("6. Back");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1": allocateBedConsole(); break;
                    case "2": releaseBedConsole(); break;
                    case "3": displayOccupiedBedsConsole(); break;
                    case "4": displayAvailableBedsConsole(); break;
                    case "5": displayAllBedsConsole(); break;
                    case "6": return;
                    default: System.out.println("Invalid option.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private void allocateBedConsole() {
        int pid = readInt("Patient ID to allocate bed to: ");
        Optional<Patient> opt = registry.findById(pid);
        if (opt.isEmpty()) { System.out.println("Patient not found."); return; }
        Patient p = opt.get();
        if (!(p instanceof Inpatient)) { System.out.println("Only inpatients may be allocated a bed."); return; }
        try {
            String bedId = bedMgmt.allocateBed(pid, registry);
            System.out.println("Allocated bed " + bedId + " to patient " + pid);
        } catch (Exception ex) {
            System.out.println("Allocation failed: " + ex.getMessage());
        }
    }

    private void releaseBedConsole() {
        System.out.print("Enter bed ID to release (e.g., B05): ");
        String bid = scanner.nextLine().trim().toUpperCase();
        boolean ok = bedMgmt.releaseBed(bid, registry);
        if (ok) System.out.println("Released bed " + bid);
        else System.out.println("Could not release bed " + bid + " (not found or already free).");
    }

    private void displayOccupiedBedsConsole() {
        System.out.println("--- Occupied Beds ---");
        List<Bed> list = bedMgmt.occupiedBeds();
        if (list.isEmpty()) System.out.println("No occupied beds.");
        else for (Bed b : list) System.out.println(b);
    }

    private void displayAvailableBedsConsole() {
        System.out.println("--- Available Beds ---");
        List<Bed> list = bedMgmt.availableBeds();
        if (list.isEmpty()) System.out.println("No available beds.");
        else for (Bed b : list) System.out.println(b.getId());
    }

    private void displayAllBedsConsole() {
        System.out.println("-- All Beds --");
        List<Bed> all = bedMgmt.allBeds();
        int cols = 5;
        for (int i = 0; i < all.size(); i++) {
            System.out.printf("%-25s", all.get(i).toString());
            if ((i + 1) % cols == 0) System.out.println();
        }
        System.out.println();
    }

    // Reports
    private void reportsMenu() {
        while (true) {
            System.out.println("--- Reports ---");
            System.out.println("1. Display all registered patients");
            System.out.println("2. Display all available beds");
            System.out.println("3. Display all occupied beds");
            System.out.println("4. Total number of registered patients");
            System.out.println("5. Total number of occupied beds");
            System.out.println("6. Ward occupancy percentage");
            System.out.println("7. Back");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": displayAllPatientsConsole(); break;
                case "2": displayAvailableBedsConsole(); break;
                case "3": displayOccupiedBedsConsole(); break;
                case "4": System.out.println("Total patients: " + registry.count()); break;
                case "5": System.out.println("Total occupied beds: " + bedMgmt.countOccupied()); break;
                case "6": System.out.printf("Occupancy: %.2f%%", bedMgmt.occupancyPercentage()); break;
                case "7": return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    // helpers for input
    private int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String line = scanner.nextLine().trim();
                return Integer.parseInt(line);
            } catch (NumberFormatException ex) { System.out.println("Enter a valid integer."); }
        }
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String readStringAllowEmpty(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}