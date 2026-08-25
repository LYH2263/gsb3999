package com.agritrace.controller;
import com.agritrace.dto.Result;
import com.agritrace.entity.HotProduct;
import com.agritrace.entity.Logistics;
import com.agritrace.entity.Product;
import com.agritrace.entity.TracingCode;
import com.agritrace.repository.HotProductRepository;
import com.agritrace.repository.LogisticsRepository;
import com.agritrace.repository.ProductRepository;
import com.agritrace.repository.TracingCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    @Autowired private HotProductRepository hotProductRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private TracingCodeRepository tracingCodeRepository;
    @Autowired private LogisticsRepository logisticsRepository;

    @GetMapping("/hot")
    public Result<?> getHotProducts() {
        List<HotProduct> hots = hotProductRepository.findByIsDisplayOrderBySearchCountDesc(1);
        List<Map<String, Object>> res = new ArrayList<>();
        for (HotProduct hp : hots) {
            Product p = productRepository.findById(hp.getProductId()).orElse(null);
            if (p != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("productName", p.getProductName());
                map.put("category", p.getCategory());
                map.put("origin", p.getOrigin());
                map.put("searchCount", hp.getSearchCount());
                res.add(map);
            }
        }
        return Result.success(res);
    }
    
    @GetMapping("/trace/{code}")
    public Result<?> traceCode(@PathVariable String code) {
        Optional<TracingCode> opt = tracingCodeRepository.findByTraceCode(code);
        if (opt.isEmpty()) return Result.error(404, "无效的溯源码");
        
        TracingCode tc = opt.get();
        Product p = productRepository.findById(tc.getProductId()).orElse(null);
        List<Logistics> logs = logisticsRepository.findByTraceCodeId(tc.getId());
        
        // update search count
        if (p != null) {
            HotProduct hp = hotProductRepository.findByProductId(p.getId()).orElse(null);
            if (hp != null) {
                hp.setSearchCount(hp.getSearchCount() + 1);
                hotProductRepository.save(hp);
            } else {
                hp = new HotProduct();
                hp.setProductId(p.getId());
                hp.setSearchCount(1);
                hp.setIsDisplay(1);
                hotProductRepository.save(hp);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("product", p);
        data.put("logistics", logs);
        data.put("traceInfo", tc);
        return Result.success(data);
    }
    
    @GetMapping("/hash")
    public Result<?> getHash() {
        return Result.success(at.favre.lib.crypto.bcrypt.BCrypt.withDefaults().hashToString(10, "123456".toCharArray()));
    }
}
