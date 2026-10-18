package com.erp.hr;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Employee master (M-HR-001). Personal data is column-encrypted at rest from v1.5 (NFR-S-07)
 * and employment history is append-only.
 */
@Entity(name = "HrEmployee")
@Table(name = "hr_employee")
public class HrEmployee extends BaseEntity {

    /**
     * employeeNumber
     */
    @Column(name = "employee_number", length = 40, nullable = false)
    public String employeeNumber;

    /**
     * firstName
     */
    @Column(name = "first_name", length = 120, nullable = false)
    public String firstName;

    /**
     * lastName
     */
    @Column(name = "last_name", length = 120)
    public String lastName;

    /**
     * fullName
     */
    @Column(name = "full_name", length = 240)
    public String fullName;

    /**
     * email
     */
    @Column(name = "email", length = 160)
    public String email;

    /**
     * phone
     */
    @Column(name = "phone", length = 40)
    public String phone;

    /**
     * dateOfBirth
     */
    @Column(name = "date_of_birth")
    public LocalDate dateOfBirth;

    /**
     * gender
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 40)
    public Gender gender;

    /**
     * KTP / NIK or national identifier.
     */
    @Column(name = "national_id", length = 40)
    public String nationalId;

    /**
     * NPWP for Indonesia; used by the PPh 21 calculation.
     */
    @Column(name = "tax_id", length = 40)
    public String taxId;

    /**
     * PPh 21 PTKP status, e.g. TK0, K1, K2, K3.
     */
    @Column(name = "tax_status", length = 20)
    public String taxStatus;

    /**
     * bpjsKesehatanNo
     */
    @Column(name = "bpjs_kesehatan_no", length = 40)
    public String bpjsKesehatanNo;

    /**
     * bpjsKetenagakerjaanNo
     */
    @Column(name = "bpjs_ketenagakerjaan_no", length = 40)
    public String bpjsKetenagakerjaanNo;

    /**
     * hireDate
     */
    @Column(name = "hire_date")
    public LocalDate hireDate;

    /**
     * terminationDate
     */
    @Column(name = "termination_date")
    public LocalDate terminationDate;

    /**
     * employmentType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 40)
    public EmploymentType employmentType;

    /**
     * status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    public EmployeeStatus status;

    /**
     * departmentId
     */
    @Column(name = "department_id")
    public Long departmentId;

    /**
     * positionId
     */
    @Column(name = "position_id")
    public Long positionId;

    /**
     * managerEmployeeId
     */
    @Column(name = "manager_employee_id")
    public Long managerEmployeeId;

    /**
     * costCenterId
     */
    @Column(name = "cost_center_id")
    public Long costCenterId;

    /**
     * AppUser created for self-service, if enabled (PRD 9).
     */
    @Column(name = "user_id")
    public Long userId;

    /**
     * basicSalary
     */
    @Column(name = "basic_salary", precision = 19, scale = 4, nullable = false)
    public BigDecimal basicSalary;

    /**
     * currency
     */
    @Column(name = "currency", length = 3)
    public String currency;

    /**
     * bankName
     */
    @Column(name = "bank_name", length = 120)
    public String bankName;

    /**
     * bankAccountNumber
     */
    @Column(name = "bank_account_number", length = 60)
    public String bankAccountNumber;

    /**
     * bankAccountHolder
     */
    @Column(name = "bank_account_holder", length = 160)
    public String bankAccountHolder;

    /**
     * addressLine1
     */
    @Column(name = "address_line1", length = 200)
    public String addressLine1;

    /**
     * city
     */
    @Column(name = "city", length = 100)
    public String city;

    /**
     * postalCode
     */
    @Column(name = "postal_code", length = 20)
    public String postalCode;

    /**
     * emergencyContactName
     */
    @Column(name = "emergency_contact_name", length = 160)
    public String emergencyContactName;

    /**
     * emergencyContactPhone
     */
    @Column(name = "emergency_contact_phone", length = 40)
    public String emergencyContactPhone;

    /**
     * notes
     */
    @Column(name = "notes")
    public String notes;

    @Override
    public String displayLabel() {
        return employeeNumber + " - " + fullName;
    }

}
