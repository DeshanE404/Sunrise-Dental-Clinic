package com.sunrise.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DentistDTO {
    private int dentistId;
    private String dentistName;
    private String specialization;

    public DentistDTO() {}

    public DentistDTO(int dentistId, String dentistName, String specialization) {
        this.dentistId = dentistId;
        this.dentistName = dentistName;
        this.specialization = specialization;
    }

    public int getDentistId() { return dentistId; }
    public void setDentistId(int dentistId) { this.dentistId = dentistId; }

    public String getDentistName() { return dentistName; }
    public void setDentistName(String dentistName) { this.dentistName = dentistName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}
