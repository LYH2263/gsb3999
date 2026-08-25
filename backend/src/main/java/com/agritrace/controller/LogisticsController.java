package com.agritrace.controller;
import com.agritrace.dto.LogisticsRequest;
import com.agritrace.dto.Result;
import com.agritrace.entity.Logistics;
import com.agritrace.entity.TracingCode;
import com.agritrace.repository.LogisticsRepository;
import com.agritrace.repository.TracingCodeRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/logistics")
public class LogisticsController {
    @Autowired private LogisticsRepository logisticsRepository;
    @Autowired private TracingCodeRepository tracingCodeRepository;

    @PostMapping("/record")
    public Result<?> addRecord(HttpServletRequest request, @RequestBody LogisticsRequest req) {
        Long adminId = ((Number)request.getAttribute("userId")).longValue();
        TracingCode tc = tracingCodeRepository.findByTraceCode(req.getTraceCode()).orElse(null);
        if (tc == null) return Result.error(404, "该溯源码未找到或无效");
        
        Logistics log = new Logistics();
        log.setTraceCodeId(tc.getId());
        log.setLogisticsAdminId(adminId);
        log.setLocation(req.getLocation());
        log.setStatusDesc(req.getStatusDesc());
        logisticsRepository.save(log);
        return Result.success(log);
    }
}
