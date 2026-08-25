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

    @PostMapping("/records")
    public Result<FarmingRecord> createRecord(HttpServletRequest request,
                                              @RequestBody FarmingRecordRequest req) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        String role = (String) request.getAttribute("role");
        return Result.success(farmingRecordService.createRecord(userId, role, req));
    }

    @GetMapping("/records/product/{productId}")
    public Result<List<FarmingRecord>> listByProduct(HttpServletRequest request,
                                                     @PathVariable Long productId) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        String role = (String) request.getAttribute("role");
        return Result.success(farmingRecordService.listRecordsByProductForFarmer(userId, role, productId));
    }

    @GetMapping("/records/my")
    public Result<List<FarmingRecord>> listMyRecords(HttpServletRequest request) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        String role = (String) request.getAttribute("role");
        return Result.success(farmingRecordService.listMyRecords(userId, role));
    }

    @GetMapping("/records/all")
    public Result<List<FarmingRecord>> listAllRecords(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        return Result.success(farmingRecordService.listAllRecords(role));
    }

    @DeleteMapping("/records/{id}")
    public Result<?> deleteRecord(HttpServletRequest request, @PathVariable Long id) {
        Long userId = ((Number) request.getAttribute("userId")).longValue();
        String role = (String) request.getAttribute("role");
        farmingRecordService.deleteRecord(userId, role, id);
        return Result.success(null);
    }
}
