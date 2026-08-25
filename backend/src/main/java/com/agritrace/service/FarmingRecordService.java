package com.agritrace.service;

import com.agritrace.dto.BusinessException;
import com.agritrace.dto.FarmingRecordRequest;
import com.agritrace.entity.FarmingRecord;
import com.agritrace.entity.Product;
import com.agritrace.repository.FarmingRecordRepository;
import com.agritrace.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class FarmingRecordService {

    @Autowired
    private FarmingRecordRepository farmingRecordRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public FarmingRecord createRecord(Long userId, String role, FarmingRecordRequest req) {
        if (!"FARMER".equals(role)) {
            throw new BusinessException(403, "仅农户可登记农事档案");
        }

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new BusinessException(404, "农产品不存在"));

        if (!product.getFarmerId().equals(userId)) {
            throw new BusinessException(403, "不能为他人产品登记农事档案");
        }

        String type = req.getOperationType();
        if (type == null || type.isBlank()) {
            throw new BusinessException(400, "操作类型不能为空");
        }
        if (!isValidOperationType(type)) {
            throw new BusinessException(400, "无效的操作类型，仅支持 SOWING / FERTILIZING / PESTICIDE");
        }

        LocalDate opDate = req.getOperationDate();
        if (opDate == null) {
            throw new BusinessException(400, "操作日期不能为空");
        }
        if (opDate.isAfter(LocalDate.now())) {
            throw new BusinessException(400, "操作日期不能晚于今天");
        }

        if ("SOWING".equals(type)) {
            if (product.getHarvestDate() != null && opDate.isAfter(product.getHarvestDate())) {
                throw new BusinessException(400, "播种日期不得晚于该产品的采摘日期");
            }
        }

        if ("PESTICIDE".equals(type)) {
            if (req.getPesticideName() == null || req.getPesticideName().isBlank()) {
                throw new BusinessException(400, "用药记录必须填写用药名称");
            }
            if (req.getSafetyIntervalDays() == null || req.getSafetyIntervalDays() < 0) {
                throw new BusinessException(400, "用药记录必须填写合法的安全间隔天数");
            }
        }

        FarmingRecord record = new FarmingRecord();
        record.setProductId(req.getProductId());
        record.setFarmerId(userId);
        record.setOperationType(type);
        record.setOperationDate(opDate);
        record.setPesticideName(req.getPesticideName());
        record.setSafetyIntervalDays(req.getSafetyIntervalDays());
        record.setRemark(req.getRemark());

        return farmingRecordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public List<FarmingRecord> listRecordsByProductForFarmer(Long userId, String role, Long productId) {
        if ("SYS_ADMIN".equals(role)) {
            return farmingRecordRepository.findByProductIdOrderByOperationDateDesc(productId);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(404, "农产品不存在"));

        if (!"FARMER".equals(role) || !product.getFarmerId().equals(userId)) {
            throw new BusinessException(403, "无权查看该产品的农事档案");
        }

        return farmingRecordRepository.findByProductIdOrderByOperationDateDesc(productId);
    }

    @Transactional(readOnly = true)
    public List<FarmingRecord> listMyRecords(Long userId, String role) {
        if (!"FARMER".equals(role)) {
            throw new BusinessException(403, "仅农户可查看自己的农事档案列表");
        }
        return farmingRecordRepository.findByFarmerIdOrderByOperationDateDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<FarmingRecord> listAllRecords(String role) {
        if (!"SYS_ADMIN".equals(role)) {
            throw new BusinessException(403, "仅系统管理员可查看全部农事档案");
        }
        return farmingRecordRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<FarmingRecord> listRecordsByProductForPublic(Long productId) {
        return farmingRecordRepository.findByProductIdOrderByOperationDateDesc(productId);
    }

    @Transactional
    public void deleteRecord(Long userId, String role, Long recordId) {
        FarmingRecord record = farmingRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(404, "农事档案记录不存在"));

        if (!"FARMER".equals(role) || !record.getFarmerId().equals(userId)) {
            throw new BusinessException(403, "只能删除自己登记的农事档案");
        }

        farmingRecordRepository.deleteById(recordId);
    }

    @Transactional(readOnly = true)
    public void assertCanGenerateTraceCode(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(404, "农产品不存在"));

        List<FarmingRecord> pesticideRecords =
                farmingRecordRepository.findByProductIdAndOperationType(productId, "PESTICIDE");

        LocalDate today = LocalDate.now();
        for (FarmingRecord r : pesticideRecords) {
            if (r.getSafetyIntervalDays() == null) continue;
            LocalDate safeUntil = r.getOperationDate().plusDays(r.getSafetyIntervalDays());
            if (!today.isAfter(safeUntil)) {
                throw new BusinessException(400,
                        String.format("该产品于 %s 使用了「%s」，安全间隔期至 %s，期间禁止生成溯源码",
                                r.getOperationDate(), r.getPesticideName(), safeUntil));
            }
        }
    }

    private boolean isValidOperationType(String type) {
        return "SOWING".equals(type) || "FERTILIZING".equals(type) || "PESTICIDE".equals(type);
    }
}
