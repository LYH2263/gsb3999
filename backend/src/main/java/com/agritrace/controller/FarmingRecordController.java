package com.agritrace.controller;

import com.agritrace.dto.FarmingRecordRequest;
import com.agritrace.dto.Result;
import com.agritrace.service.FarmingRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 农事档案接口：仅做鉴权信息提取与入参转发，业务规则全部在 FarmingRecordService。
 * 访问控制：/api/farming 仅 FARMER / SYS_ADMIN 可进入（见 SecurityInterceptor）。
 */
@RestController
@RequestMapping("/api/farming")
public class FarmingRecordController {
    @Autowired private FarmingRecordService farmingRecordService;

    @PostMapping("/records")
    public Result<?> create(HttpServletRequest request, @RequestBody FarmingRecordRequest req) {
        return Result.success(farmingRecordService.create(userId(request), role(request), req));
    }

    @GetMapping("/records/my")
    public Result<?> my(HttpServletRequest request) {
        return Result.success(farmingRecordService.listMine(userId(request), role(request)));
    }

    @GetMapping("/records/product/{productId}")
    public Result<?> byProduct(HttpServletRequest request, @PathVariable Long productId) {
        return Result.success(farmingRecordService.listByProduct(userId(request), role(request), productId));
    }

    private Long userId(HttpServletRequest request) {
        return ((Number) request.getAttribute("userId")).longValue();
    }

    private String role(HttpServletRequest request) {
        return (String) request.getAttribute("role");
    }
}
