package com.jdt16.agenin.users.service.implementation.module;

import com.jdt16.agenin.users.dto.request.LogRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogProducerService {

    private final KafkaTemplate<String, LogRequestDTO> auditLogKafkaTemplate;

    @Value("${kafka.audit-log-topic:create-log-po}")
    private String auditLogTopic;

    /**
     * Mengirim audit log ke Kafka untuk operasi CREATE
     */
    public void logCreate(String tableName, UUID recordId, Map<String, Object> newData,
                          UUID userId, String userFullname, UUID roleId, String roleName,
                          String userAgent, String ipAddress) {

        LogRequestDTO logRequest = LogRequestDTO.builder()
                .logEntityDTOAuditLogsId(UUID.randomUUID())
                .logEntityDTOTableName(tableName)
                .logEntityDTORecordId(recordId)
                .logEntityDTOAction("CREATE")
                .logEntityDTOOldData(Map.of())
                .logEntityDTONewData(newData)
                .logEntityDTOUserId(userId)
                .logEntityDTOUserFullname(userFullname)
                .logEntityDTORoleId(roleId)
                .logEntityDTORoleName(roleName)
                .logEntityDTOUserAgent(userAgent)
                .logEntityDTOIpAddress(ipAddress)
                .logEntityDTOChangedAt(LocalDateTime.now())
                .build();

        sendAuditLog(logRequest);
    }

    /**
     * Mengirim audit log ke Kafka untuk operasi UPDATE
     */
    public void logUpdate(String tableName, UUID recordId,
                          Map<String, Object> oldData, Map<String, Object> newData,
                          UUID userId, String userFullname, UUID roleId, String roleName,
                          String userAgent, String ipAddress) {

        LogRequestDTO logRequest = LogRequestDTO.builder()
                .logEntityDTOAuditLogsId(UUID.randomUUID())
                .logEntityDTOTableName(tableName)
                .logEntityDTORecordId(recordId)
                .logEntityDTOAction("UPDATE")
                .logEntityDTOOldData(oldData)
                .logEntityDTONewData(newData)
                .logEntityDTOUserId(userId)
                .logEntityDTOUserFullname(userFullname)
                .logEntityDTORoleId(roleId)
                .logEntityDTORoleName(roleName)
                .logEntityDTOUserAgent(userAgent)
                .logEntityDTOIpAddress(ipAddress)
                .logEntityDTOChangedAt(LocalDateTime.now())
                .build();

        sendAuditLog(logRequest);
    }

    /**
     * Mengirim audit log ke Kafka untuk operasi DELETE
     */
    public void logDelete(String tableName, UUID recordId, Map<String, Object> oldData,
                          UUID userId, String userFullname, UUID roleId, String roleName,
                          String userAgent, String ipAddress) {

        LogRequestDTO logRequest = LogRequestDTO.builder()
                .logEntityDTOAuditLogsId(UUID.randomUUID())
                .logEntityDTOTableName(tableName)
                .logEntityDTORecordId(recordId)
                .logEntityDTOAction("DELETE")
                .logEntityDTOOldData(oldData)
                .logEntityDTONewData(Map.of())  // Empty untuk DELETE
                .logEntityDTOUserId(userId)
                .logEntityDTOUserFullname(userFullname)
                .logEntityDTORoleId(roleId)
                .logEntityDTORoleName(roleName)
                .logEntityDTOUserAgent(userAgent)
                .logEntityDTOIpAddress(ipAddress)
                .logEntityDTOChangedAt(LocalDateTime.now())
                .build();

        sendAuditLog(logRequest);
    }

    /**
     * Method internal untuk send ke Kafka dengan key partitioning
     */
    private void sendAuditLog(LogRequestDTO logRequest) {
        // Key format: tableName#recordId untuk menjaga ordering per entitas
        String key = logRequest.getLogEntityDTOTableName() + "#" + logRequest.getLogEntityDTORecordId();

        CompletableFuture<SendResult<String, LogRequestDTO>> future =
                auditLogKafkaTemplate.send(auditLogTopic, key, logRequest);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Audit log sent successfully: ID={}, table={}, action={}, partition={}, offset={}",
                        logRequest.getLogEntityDTOAuditLogsId(),
                        logRequest.getLogEntityDTOTableName(),
                        logRequest.getLogEntityDTOAction(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send audit log: ID={}, table={}, action={}",
                        logRequest.getLogEntityDTOAuditLogsId(),
                        logRequest.getLogEntityDTOTableName(),
                        logRequest.getLogEntityDTOAction(),
                        ex);
            }
        });
    }
}
