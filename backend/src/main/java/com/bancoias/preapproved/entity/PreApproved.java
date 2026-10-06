package com.bancoias.preapproved.entity;

import com.bancoias.preapproved.model.PreApprovedStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

//Basicamente aqui se configura la estructura de la tabla de pre aprobados
@Table("pre_approved")
public class PreApproved {

    @Id
    @Column("id")
    private String id;

    @Column("customer_id")
    private String customerId;

    @Column("status")
    private PreApprovedStatus status;

    @Column("available_amount")
    private BigDecimal availableAmount;

    @Column("version")
    private Long version;

    public PreApproved() {
    }

    public PreApproved(String id, String customerId, PreApprovedStatus status, BigDecimal availableAmount, Long version) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.availableAmount = availableAmount;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public PreApprovedStatus getStatus() {
        return status;
    }

    public void setStatus(PreApprovedStatus status) {
        this.status = status;
    }

    public BigDecimal getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(BigDecimal availableAmount) {
        this.availableAmount = availableAmount;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
