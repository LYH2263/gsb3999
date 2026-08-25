package com.agritrace.controller;

import com.agritrace.dto.FarmingRecordRequest;
import com.agritrace.dto.Result;
import com.agritrace.entity.FarmingRecord;
import com.agritrace.service.FarmingRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 农事档案接口层：只做鉴权上下文提取与入参转发，
 * 所有业务规则（越权、日期、安全间隔）均下沉到 FarmingRecordService。
 */
@RestController
@RequestMapping("/api/farming")
public class FarmingRecordController {

    @Autowired
    private FarmingRecordService farmingRecordService;

    @PostMapping("/records")
    public Result<FarmingRecord> createRecord(HttpServletRequest request,
                                              @RequestBody FarmingRecordRequest req) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        String role = (String) request.getAttribute("role");
        return Result.success(farmingRecordService.createRecord(userId, role, req));
    }

    @GetMapping("/records/my")
    public Result<List<FarmingRecord>> myRecords(HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        return Result.success(farmingRecordService.getMyRecords(userId));
    }

    @GetMapping("/records/product/{productId}")
    public Result<List<FarmingRecord>> recordsByProduct(HttpServletRequest request,
                                                        @PathVariable Long productId) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        String role = (String) request.getAttribute("role");
        return Result.success(farmingRecordService.getRecordsByProduct(userId, role, productId));
    }
}
