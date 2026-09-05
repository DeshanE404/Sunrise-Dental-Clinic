package com.sunrise.model;

public class DentistWorkloadReport {
    private String dentistName;
    private long totalAppointments;
    private long completedAppointments;
    private long scheduledAppointments;
    private long cancelledAppointments;
    private long noShowAppointments;

    public String getDentistName() { return dentistName; }
    public void setDentistName(String dentistName) { this.dentistName = dentistName; }

    public long getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(long totalAppointments) { this.totalAppointments = totalAppointments; }

    public long getCompletedAppointments() { return completedAppointments; }
    public void setCompletedAppointments(long completedAppointments) { this.completedAppointments = completedAppointments; }

    public long getScheduledAppointments() { return scheduledAppointments; }
    public void setScheduledAppointments(long scheduledAppointments) { this.scheduledAppointments = scheduledAppointments; }

    public long getCancelledAppointments() { return cancelledAppointments; }
    public void setCancelledAppointments(long cancelledAppointments) { this.cancelledAppointments = cancelledAppointments; }

    public long getNoShowAppointments() { return noShowAppointments; }
    public void setNoShowAppointments(long noShowAppointments) { this.noShowAppointments = noShowAppointments; }
}
