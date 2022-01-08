package ir.rahyab.hermes.adm.subscribersadmin.subscribersadmin.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * The {@code AddingAuditTrailDto} class , used for data transferring between {@code AuditTrailManagementSystem} and support user,
 *
 * also used for saving changes about msisdn information's operations in audit trail management system
 *
 * @author tahbaz
 */



public class AddingAuditTrailDto implements Serializable {

    private static final long serialVersionUID = -3326944692504056321L;

    @JsonIgnore
    private String id;

    //@ApiModelProperty(notes = "User of audit trail should be maximom 100 characters , this parameter is required.")
    private String auditUser;

    @JsonIgnore
    private String auditClientIp;

    @JsonIgnore
    private String auditServerIp;

    //@ApiModelProperty(notes = "Resource of audit trail should be maximom 100 characters , this parameter is required.")
    private String auditResource;

    //@ApiModelProperty(notes = "Action of audit trail should be maximom 100 characters , this parameter is required.")
    private String auditAction;

    //@ApiModelProperty(notes = "ApplicCd should be maximom 30 characters , this parameter is required.")
    private String applicCd;

    //@ApiModelProperty(notes = "Last value should be maximom 1000 characters , this parameter is required.")
    private String lastValue;

    @JsonIgnore
    private LocalDateTime auditDate;

    @JsonIgnore
    private LocalDateTime startAuditDate;

    @JsonIgnore
    private LocalDateTime endAuditDate;

    //@ApiModelProperty(notes = "Description should be maximom 200 characters , this parameter is required.")
    private String description;

    //@ApiModelProperty(notes = "Refrence should be maximom 128 characters , this parameter is required.")
    private String refrence;

    private Long serial_number;

    public AddingAuditTrailDto(String id, String auditUser, String auditClientIp, String auditServerIp, String auditResource, String auditAction, String applicCd, String lastValue, LocalDateTime auditDate, LocalDateTime startAuditDate, LocalDateTime endAuditDate, String description, String refrence, Long serial_number) {
        this.id = id;
        this.auditUser = auditUser;
        this.auditClientIp = auditClientIp;
        this.auditServerIp = auditServerIp;
        this.auditResource = auditResource;
        this.auditAction = auditAction;
        this.applicCd = applicCd;
        this.lastValue = lastValue;
        this.auditDate = auditDate;
        this.startAuditDate = startAuditDate;
        this.endAuditDate = endAuditDate;
        this.description = description;
        this.refrence = refrence;
        this.serial_number = serial_number;
    }

    public AddingAuditTrailDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuditUser() {
        return auditUser;
    }

    public void setAuditUser(String auditUser) {
        this.auditUser = auditUser;
    }

    public String getAuditClientIp() {
        return auditClientIp;
    }

    public void setAuditClientIp(String auditClientIp) {
        this.auditClientIp = auditClientIp;
    }

    public String getAuditServerIp() {
        return auditServerIp;
    }

    public void setAuditServerIp(String auditServerIp) {
        this.auditServerIp = auditServerIp;
    }

    public String getAuditResource() {
        return auditResource;
    }

    public void setAuditResource(String auditResource) {
        this.auditResource = auditResource;
    }

    public String getAuditAction() {
        return auditAction;
    }

    public void setAuditAction(String auditAction) {
        this.auditAction = auditAction;
    }

    public String getApplicCd() {
        return applicCd;
    }

    public void setApplicCd(String applicCd) {
        this.applicCd = applicCd;
    }

    public String getLastValue() {
        return lastValue;
    }

    public void setLastValue(String lastValue) {
        this.lastValue = lastValue;
    }

    public LocalDateTime getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(LocalDateTime auditDate) {
        this.auditDate = auditDate;
    }

    public LocalDateTime getStartAuditDate() {
        return startAuditDate;
    }

    public void setStartAuditDate(LocalDateTime startAuditDate) {
        this.startAuditDate = startAuditDate;
    }

    public LocalDateTime getEndAuditDate() {
        return endAuditDate;
    }

    public void setEndAuditDate(LocalDateTime endAuditDate) {
        this.endAuditDate = endAuditDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRefrence() {
        return refrence;
    }

    public void setRefrence(String refrence) {
        this.refrence = refrence;
    }

    public Long getSerial_number() {
        return serial_number;
    }

    public void setSerial_number(Long serial_number) {
        this.serial_number = serial_number;
    }
}
