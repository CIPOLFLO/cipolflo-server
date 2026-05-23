package com.cipolflo.server.shared.dto;

import lombok.Getter;

import java.time.Instant;

@Getter
public class AuditInfoDto {

    private final Instant createdAt;
    private final Instant updatedAt;
    private final String createdBy;
    private final String updatedBy;

    protected AuditInfoDto(Instant createdAt, Instant updatedAt, String createdBy, String updatedBy) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
}
