package com.bancoias.preapproved.entity;

import com.bancoias.preapproved.model.RequestStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//Aqui se configura la estructura de la tabla del historial de solicitudes
@Table("usage_request")
public class UsageRequest {

    @Id
    @Column("id")
    private Long id;

    @Column("request_reference")
    private String requestReference;

    @Column("pre_approved_id")
    private String preApprovedId;

    @Column("customer_id")
    private String customerId;

    @Column("amount")
    private BigDecimal amount;

    @Column("status")
    private RequestStatus status;

    @Column("rejection_reason")
    private String rejectionReason;

    @Column("processed_at")
    private LocalDateTime processedAt;

    public UsageRequest() {
    }

    public UsageRequest(Long id, String requestReference, String preApprovedId, String customerId,
                        BigDecimal amount, RequestStatus status, String rejectionReason, LocalDateTime processedAt) {
        this.id = id;
        this.requestReference = requestReference;
        this.preApprovedId = preApprovedId;
        this.customerId = customerId;
        this.amount = amount;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.processedAt = processedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestReference() {
        return requestReference;
    }

    public void setRequestReference(String requestReference) {
        this.requestReference = requestReference;
    }

    public String getPreApprovedId() {
        return preApprovedId;
    }

    public void setPreApprovedId(String preApprovedId) {
        this.preApprovedId = preApprovedId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
