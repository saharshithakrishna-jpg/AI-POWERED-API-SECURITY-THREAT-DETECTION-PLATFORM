package com.apisecurity.repository;

import com.apisecurity.model.ApiRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApiRequestRepository extends JpaRepository<ApiRequest, Long> {
    
    List<ApiRequest> findBySourceIpAndTimestampAfter(String sourceIp, LocalDateTime timestamp);
    
    Long countBySourceIpAndTimestampAfter(String sourceIp, LocalDateTime timestamp);
    
    @Query("SELECT COUNT(a) FROM ApiRequest a WHERE a.sourceIp = :sourceIp AND a.responseStatus >= 400 AND a.timestamp > :timestamp")
    Long countFailedRequestsByIpInWindow(@Param("sourceIp") String sourceIp, @Param("timestamp") LocalDateTime timestamp);
    
    @Query("SELECT COUNT(a) FROM ApiRequest a WHERE a.sourceIp = :sourceIp AND a.responseStatus IN (401, 403) AND a.timestamp > :timestamp")
    Long countAuthFailuresByIpInWindow(@Param("sourceIp") String sourceIp, @Param("timestamp") LocalDateTime timestamp);
    
    @Query("SELECT COUNT(DISTINCT a.endpoint) FROM ApiRequest a WHERE a.sourceIp = :sourceIp AND a.timestamp > :timestamp")
    Long countUniqueEndpointsByIpInWindow(@Param("sourceIp") String sourceIp, @Param("timestamp") LocalDateTime timestamp);
    
    @Query("SELECT AVG(a.responseTime) FROM ApiRequest a WHERE a.sourceIp = :sourceIp AND a.timestamp > :timestamp")
    Double getAverageResponseTimeByIpInWindow(@Param("sourceIp") String sourceIp, @Param("timestamp") LocalDateTime timestamp);
}
