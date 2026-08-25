package com.agritrace.service;

import com.agritrace.dto.BizException;
import com.agritrace.dto.FarmingRecordRequest;
import com.agritrace.entity.FarmingRecord;
import com.agritrace.entity.Product;
import com.agritrace.repository.FarmingRecordRepository;
import com.agritrace.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * 农事档案业务层：承担全部业务规则（越权判断、播种/采摘日期校验、
 * 用药必填校验、安全间隔期计算），Controller 仅做鉴权与入参转发。
 */
@Service
public class FarmingRecordService {
    private static final Set<String> OPERATION_TYPES = Set.of("SOWING", "FERTILIZING", "PESTICIDE");

    @Autowired private FarmingRecordRepository farmingRecordRepository;
    @Autowired private ProductRepository productRepository;

    /** 农户登记档案：仅农户本人可登记（管理员不可代录），且只能登记自己的产品 */
    public FarmingRecord create(Long userId, String role, FarmingRecordRequest req) {
        if (!"FARMER".equals(role)) {
            throw new BizException(403, "仅农户本人可登记农事档案，管理员不可代农户新增");
        }
        if (req.getProductId() == null) {
            throw new BizException(400, "请选择关联的农产品");
        }
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new BizException(404, "该农产品不存在"));
        if (!product.getFarmerId().equals(userId)) {
            throw new BizException(403, "只能为自己的产品登记农事档案");
        }
        if (req.getOperationType() == null || !OPERATION_TYPES.contains(req.getOperationType())) {
            throw new BizException(400, "操作类型必须为 播种/施肥/用药");
        }
        if (req.getOperationDate() == null) {
            throw new BizException(400, "操作日期不能为空");
        }
        // 播种日期不得晚于该产品采摘日期
        if ("SOWING".equals(req.getOperationType())
                && product.getHarvestDate() != null
                && req.getOperationDate().isAfter(product.getHarvestDate())) {
            throw new BizException(400, "播种日期不得晚于该产品采摘日期(" + product.getHarvestDate() + ")");
        }

        FarmingRecord record = new FarmingRecord();
        record.setProductId(product.getId());
        record.setFarmerId(userId);
        record.setOperationType(req.getOperationType());
        record.setOperationDate(req.getOperationDate());
        if ("PESTICIDE".equals(req.getOperationType())) {
            if (req.getDrugName() == null || req.getDrugName().isBlank()) {
                throw new BizException(400, "用药记录必须填写用药名称");
            }
            if (req.getSafetyIntervalDays() == null || req.getSafetyIntervalDays() < 0) {
                throw new BizException(400, "用药记录必须填写安全间隔天数(≥0)");
            }
            record.setDrugName(req.getDrugName());
            record.setSafetyIntervalDays(req.getSafetyIntervalDays());
        }
        record.setRemark(req.getRemark());
        return farmingRecordRepository.save(record);
    }

    /** 农户查自己全部档案；系统管理员查全部档案 */
    public List<FarmingRecord> listMine(Long userId, String role) {
        if ("SYS_ADMIN".equals(role)) {
            return farmingRecordRepository.findAll();
        }
        return farmingRecordRepository.findByFarmerIdOrderByOperationDateDesc(userId);
    }

    /** 按产品查档案：农户限本人产品，管理员不限 */
    public List<FarmingRecord> listByProduct(Long userId, String role, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BizException(404, "该农产品不存在"));
        if (!"SYS_ADMIN".equals(role) && !product.getFarmerId().equals(userId)) {
            throw new BizException(403, "没有查看该产品农事档案的权限");
        }
        return farmingRecordRepository.findByProductIdOrderByOperationDateAsc(productId);
    }

    /** 消费者溯源只读视图：按产品公开全部档案 */
    public List<FarmingRecord> listForTrace(Long productId) {
        return farmingRecordRepository.findByProductIdOrderByOperationDateAsc(productId);
    }

    /** 生成溯源码前校验：存在用药记录且当前日期仍处于 操作日期+安全间隔天数 内则阻断 */
    public void assertTraceCodeAllowed(Long productId) {
        List<FarmingRecord> pesticideRecords =
                farmingRecordRepository.findByProductIdAndOperationType(productId, "PESTICIDE");
        LocalDate today = LocalDate.now();
        for (FarmingRecord record : pesticideRecords) {
            if (record.getOperationDate() == null || record.getSafetyIntervalDays() == null) {
                continue;
            }
            LocalDate safetyEnd = record.getOperationDate().plusDays(record.getSafetyIntervalDays());
            if (!today.isAfter(safetyEnd)) {
                throw new BizException(409,
                        "该产品存在用药记录(" + record.getDrugName() + "，用药日期 "
                                + record.getOperationDate() + ")，安全间隔期至 " + safetyEnd
                                + " 止，间隔期内禁止生成溯源码");
            }
        }
    }
}
