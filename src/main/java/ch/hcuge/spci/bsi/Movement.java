package ch.hcuge.spci.bsi;


import java.time.LocalDate;

public class Movement {
    String movementId;
    String patientId;
    String wardId;
    String wardECDCWardClassification;
    LocalDate admWardDate;
    LocalDate disWardDate;
    LocalDate admHospDate;
    LocalDate disHospDate;

    public Movement(String movementId, String patientId, String wardId, String wardECDCWardClassification, LocalDate admWardDate, LocalDate disWardDate, LocalDate admHospDate, LocalDate disHospDate) {
        this.movementId = movementId;
        this.patientId = patientId;
        this.wardId = wardId;
        this.wardECDCWardClassification = wardECDCWardClassification;
        this.admWardDate = admWardDate;
        this.disWardDate = disWardDate;
        this.admHospDate = admHospDate;
        this.disHospDate = disHospDate;
    }

    public String getMovementId() {
        return movementId;
    }

    public void setMovementId(String movementId) {
        this.movementId = movementId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getWardId() {
        return wardId;
    }

    public void setWardId(String wardId) {
        this.wardId = wardId;
    }

    public String getWardECDCWardClassification() {
        return wardECDCWardClassification;
    }

    public void setWardECDCWardClassification(String wardECDCWardClassification) {
        this.wardECDCWardClassification = wardECDCWardClassification;
    }

    public LocalDate getAdmWardDate() {
        return admWardDate;
    }

    public void setAdmWardDate(LocalDate admWardDate) {
        this.admWardDate = admWardDate;
    }

    public LocalDate getDisWardDate() {
        return disWardDate;
    }

    public void setDisWardDate(LocalDate disWardDate) {
        this.disWardDate = disWardDate;
    }

    public LocalDate getAdmHospDate() {
        return admHospDate;
    }

    public void setAdmHospDate(LocalDate admHospDate) {
        this.admHospDate = admHospDate;
    }

    public LocalDate getDisHospDate() {
        return disHospDate;
    }

    public void setDisHospDate(LocalDate disHospDate) {
        this.disHospDate = disHospDate;
    }

}
