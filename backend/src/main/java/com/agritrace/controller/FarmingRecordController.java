package com.agritrace.controller;

import com.agritrace.dto.FarmingRecordRequest;
import com.agritrace.dto.Result;
import com.agritrace.entity.FarmingRecord;
import com.agritrace.service.FarmingRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farming")
public class FarmingRecordController {

    @Autowired
    private FarmingRecordService farmingRecordService;

    @PostMapping("/record")
    public Result<FarmingRecord> addRecord(HttpServletRequest request, @RequestBody FarmingRecordRequest req) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        String role = (String) request.getAttribute("role");
        return Result.success(farmingRecordService.addRecord(userId, role, req));
    }

    @GetMapping("/records")
    public Result<List<FarmingRecord>> listRecords(HttpServletRequest request,
                                                   @RequestParam(required = false) Long productId) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        String role = (String) request.getAttribute("role");
        return Result.success(farmingRecordService.listRecords(userId, role, productId));
    }
}
