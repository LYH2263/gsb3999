package com.agritrace.controller;
import com.agritrace.dto.Result;
import com.agritrace.entity.Product;
import com.agritrace.entity.TracingCode;
import com.agritrace.repository.HotProductRepository;
import com.agritrace.repository.ProductRepository;
import com.agritrace.repository.TracingCodeRepository;
import com.agritrace.service.FarmingRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/farmer")
public class FarmerController {
    @Autowired private ProductRepository productRepository;
    @Autowired private TracingCodeRepository tracingCodeRepository;
    @Autowired private HotProductRepository hotProductRepository;
    @Autowired private FarmingRecordService farmingRecordService;

    @GetMapping("/products")
    public Result<List<Product>> getProducts(HttpServletRequest request) {
        Long userId = ((Number)request.getAttribute("userId")).longValue();
        String role = (String) request.getAttribute("role");
        if ("SYS_ADMIN".equals(role)) {
            return Result.success(productRepository.findAll());
        }
        return Result.success(productRepository.findByFarmerId(userId));
    }

    @PostMapping("/product")
    public Result<?> addProduct(HttpServletRequest request, @RequestBody Product product) {
        Long userId = ((Number)request.getAttribute("userId")).longValue();
        product.setFarmerId(userId);
        productRepository.save(product);
        return Result.success(product);
    }
    
    @DeleteMapping("/product/{id}")
    public Result<?> deleteProduct(HttpServletRequest request, @PathVariable Long id) {
        Long userId = ((Number)request.getAttribute("userId")).longValue();
        String role = (String) request.getAttribute("role");
        Product p = productRepository.findById(id).orElse(null);
        if (p == null) return Result.error(404, "该农产品不存在");
        if (!"SYS_ADMIN".equals(role) && !p.getFarmerId().equals(userId)) {
            return Result.error(403, "没有该操作的权限");
        }
        productRepository.deleteById(id);
        return Result.success(null);
    }

    @PostMapping("/trace_code/{productId}")
    public Result<?> generateTraceCode(HttpServletRequest request, @PathVariable Long productId) {
        Long userId = ((Number)request.getAttribute("userId")).longValue();
        String role = (String) request.getAttribute("role");
        Product p = productRepository.findById(productId).orElse(null);
        if (p == null) return Result.error(404, "农产品未找到");
        if (!"SYS_ADMIN".equals(role) && !p.getFarmerId().equals(userId)) {
            return Result.error(403, "没有该操作的权限");
        }

        farmingRecordService.assertCanGenerateTraceCode(productId);

        TracingCode tc = new TracingCode();
        tc.setProductId(productId);
        tc.setTraceCode(UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        tc.setStatus(1);
        tracingCodeRepository.save(tc);
        return Result.success(tc);
    }
    
    @GetMapping("/trace_code/list/{productId}")
    public Result<List<TracingCode>> getTracingCodes(@PathVariable Long productId) {
        return Result.success(tracingCodeRepository.findByProductId(productId));
    }
}
