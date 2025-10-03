# MySQL Schema Design: Smart Clinic

## Tables

### Patients
- `patient_id` INT PRIMARY KEY AUTO_INCREMENT
- `name` VARCHAR(100)
- `email` VARCHAR(100) UNIQUE
- `phone` VARCHAR(15)

### Doctors
- `doctor_id` INT PRIMARY KEY AUTO_INCREMENT
- `name` VARCHAR(100)
- `specialization` VARCHAR(100)
- `availability` TEXT

### Appointments
- `appointment_id` INT PRIMARY KEY AUTO_INCREMENT
- `patient_id` INT FOREIGN KEY REFERENCES Patients(patient_id)
- `doctor_id` INT FOREIGN KEY REFERENCES Doctors(doctor_id)
- `appointment_date` DATETIME
- `status` ENUM('Scheduled', 'Completed', 'Cancelled')
