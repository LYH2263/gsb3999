package com.agritrace.service;

import com.agritrace.dto.FarmingRecordRequest;
import com.agritrace.entity.FarmingRecord;
import com.agritrace.entity.Product;
import com.agritrace.entity.TracingCode;
import com.agritrace.exception.BusinessException;
import com.agritrace.repository.FarmingRecordRepository;
import com.agritrace.repository.ProductRepository;
import com.agritrace.repository.TracingCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FarmingRecordService {

    public static final String TYPE_SOWING = "SOWING";
    public static final String TYPE_FERTILIZING = "FERTILIZING";
    public static final String TYPE_PESTICIDE = "PESTICIDE";

    private static final Set<String> OPERATION_TYPES = Set.of(TYPE_SOWING, TYPE_FERTILIZING, TYPE_PESTICIDE);

    @Autowired
    private FarmingRecordRepository farmingRecordRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TracingCodeRepository tracingCodeRepository;

    public FarmingRecord addRecord(Long userId, String role, FarmingRecordRequest req) {
        if (!"FARMER".equals(role)) {
            throw new BusinessException(403, "仅农户可录入农事档案，管理员不可代农户新增");
        }
        if (req == null || req.getProductId() == null) {
            throw new BusinessException(400, "请选择关联农产品");
        }
        Product product = productRepository.findById(req.getProductId()).orElse(null);
        if (product == null) {
            throw new BusinessException(404, "该农产品不存在");
        }
        if (!product.getFarmerId().equals(userId)) {
            throw new BusinessException(403, "只能为自己名下的农产品登记农事档案");
        }
        if (req.getOperationType() == null || !OPERATION_TYPES.contains(req.getOperationType())) {
            throw new BusinessException(400, "操作类型不合法，仅支持: 播种 / 施肥 / 用药");
        }
        if (req.getOperationDate() == null) {
            throw new BusinessException(400, "操作日期不能为空");
        }
        if (req.getOperationDate().isAfter(LocalDate.now())) {
            throw new BusinessException(400, "操作日期不能晚于当前日期");
        }

        if (TYPE_SOWING.equals(req.getOperationType())) {
            if (product.getHarvestDate() != null && req.getOperationDate().isAfter(product.getHarvestDate())) {
                throw new BusinessException(400, "播种日期不得晚于该产品的采摘日期(" + product.getHarvestDate() + ")");
            }
        } else {
            if (req.getMaterialName() == null || req.getMaterialName().trim().isEmpty()) {
                String label = TYPE_PESTICIDE.equals(req.getOperationType()) ? "用药名称" : "肥料名称";
                throw new BusinessException(400, label + "不能为空");
            }
        }
        if (TYPE_PESTICIDE.equals(req.getOperationType())) {
            if (req.getSafetyIntervalDays() == null || req.getSafetyIntervalDays() < 0) {
                throw new BusinessException(400, "用药记录必须填写不小于 0 的安全间隔天数");
            }
        }

        FarmingRecord record = new FarmingRecord();
        record.setProductId(product.getId());
        record.setFarmerId(userId);
        record.setOperationType(req.getOperationType());
        record.setOperationDate(req.getOperationDate());
        record.setMaterialName(req.getMaterialName() == null ? null : req.getMaterialName().trim());
        record.setSafetyIntervalDays(TYPE_PESTICIDE.equals(req.getOperationType()) ? req.getSafetyIntervalDays() : null);
        record.setRemark(req.getRemark());
        return farmingRecordRepository.save(record);
    }

    public List<FarmingRecord> listRecords(Long userId, String role, Long productId) {
        if (productId != null) {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                throw new BusinessException(404, "该农产品不存在");
            }
            if ("FARMER".equals(role) && !product.getFarmerId().equals(userId)) {
                throw new BusinessException(403, "只能查看自己名下农产品的农事档案");
            }
            return farmingRecordRepository.findByProductIdOrderByOperationDateDescIdDesc(productId);
        }
        if ("SYS_ADMIN".equals(role)) {
            return farmingRecordRepository.findAll();
        }
        return farmingRecordRepository.findByFarmerIdOrderByOperationDateDescIdDesc(userId);
    }

    public List<FarmingRecord> listForTrace(Long productId) {
        return farmingRecordRepository.findByProductIdOrderByOperationDateDescIdDesc(productId);
    }

    public TracingCode generateTraceCode(Long userId, String role, Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            throw new BusinessException(404, "农产品未找到");
        }
        if (!"SYS_ADMIN".equals(role) && !product.getFarmerId().equals(userId)) {
            throw new BusinessException(403, "没有该操作的权限");
        }
        assertSafetyIntervalPassed(productId);

        TracingCode tc = new TracingCode();
        tc.setProductId(productId);
        tc.setTraceCode(UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        tc.setStatus(1);
        return tracingCodeRepository.save(tc);
    }

    private void assertSafetyIntervalPassed(Long productId) {
        List<FarmingRecord> pesticideRecords =
                farmingRecordRepository.findByProductIdAndOperationTypeOrderByOperationDateDescIdDesc(productId, TYPE_PESTICIDE);
        LocalDate today = LocalDate.now();
        for (FarmingRecord record : pesticideRecords) {
            int intervalDays = record.getSafetyIntervalDays() == null ? 0 : record.getSafetyIntervalDays();
            LocalDate safeDate = record.getOperationDate().plusDays(intervalDays);
            if (!today.isAfter(safeDate)) {
                throw new BusinessException(400,
                        "该产品存在用药记录「" + record.getMaterialName() + "」(" + record.getOperationDate()
                                + " 施用，安全间隔 " + intervalDays + " 天)，当前仍处于安全间隔期内，"
                                + safeDate.plusDays(1) + " 起方可生成溯源码");
            }
        }
    }
}
