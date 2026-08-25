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
 * 农事档案模块唯一业务承载层。
 * 所有越权判断、日期校验、安全间隔计算等业务规则均在此实现，
 * Controller 仅负责鉴权与入参转发，禁止在其中编写业务逻辑。
 */
@Service
public class FarmingRecordService {

    private static final String ROLE_FARMER = "FARMER";
    private static final String ROLE_SYS_ADMIN = "SYS_ADMIN";

    private static final String OP_SOWING = "SOWING";
    private static final String OP_FERTILIZING = "FERTILIZING";
    private static final String OP_PESTICIDE = "PESTICIDE";
    private static final Set<String> VALID_OPS = Set.of(OP_SOWING, OP_FERTILIZING, OP_PESTICIDE);

    @Autowired
    private FarmingRecordRepository farmingRecordRepository;
    @Autowired
    private ProductRepository productRepository;

    /**
     * 农户为自己的产品登记一条农事记录。
     * 业务规则：
     *  - 仅 FARMER 可新增；管理员即使能查看全部档案也不得代农户新增。
     *  - 农户只能操作自己的产品。
     *  - 播种记录的操作日期不得晚于该产品的采摘日期。
     *  - 用药记录必须提供用药名称与安全间隔天数。
     */
    public FarmingRecord createRecord(Long userId, String role, FarmingRecordRequest req) {
        if (!ROLE_FARMER.equals(role)) {
            throw new BizException(403, "仅农户可登记农事档案，管理员不可代农户新增");
        }
        if (req.getProductId() == null) {
            throw new BizException(400, "缺少产品ID");
        }
        String op = req.getOperationType();
        if (op == null || !VALID_OPS.contains(op)) {
            throw new BizException(400, "非法的操作类型");
        }
        if (req.getOperationDate() == null) {
            throw new BizException(400, "操作日期不能为空");
        }

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new BizException(404, "该农产品不存在"));
        if (!product.getFarmerId().equals(userId)) {
            throw new BizException(403, "只能为自己的农产品登记档案");
        }

        // 播种日期不得晚于该产品采摘日期
        if (OP_SOWING.equals(op) && product.getHarvestDate() != null
                && req.getOperationDate().isAfter(product.getHarvestDate())) {
            throw new BizException(400, "播种日期不得晚于该产品采摘日期");
        }

        // 用药记录必须携带用药名称与安全间隔天数
        if (OP_PESTICIDE.equals(op)) {
            if (req.getDrugName() == null || req.getDrugName().trim().isEmpty()) {
                throw new BizException(400, "用药记录必须填写用药名称");
            }
            if (req.getSafetyIntervalDays() == null || req.getSafetyIntervalDays() < 0) {
                throw new BizException(400, "用药记录必须填写合法的安全间隔天数");
            }
        }

        FarmingRecord record = new FarmingRecord();
        record.setProductId(req.getProductId());
        record.setFarmerId(userId);
        record.setOperationType(op);
        record.setOperationDate(req.getOperationDate());
        // 非用药记录不保存药名/间隔天数，避免脏数据
        if (OP_PESTICIDE.equals(op)) {
            record.setDrugName(req.getDrugName().trim());
            record.setSafetyIntervalDays(req.getSafetyIntervalDays());
        }
        record.setRemark(req.getRemark());
        return farmingRecordRepository.save(record);
    }

    /** 农户查看自己名下全部农事档案。 */
    public List<FarmingRecord> getMyRecords(Long userId) {
        return farmingRecordRepository.findByFarmerIdOrderByOperationDateDesc(userId);
    }

    /**
     * 按产品查看农事档案。
     *  - 农户只能查看自己产品的档案。
     *  - 管理员可查看任意产品的档案。
     */
    public List<FarmingRecord> getRecordsByProduct(Long userId, String role, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BizException(404, "该农产品不存在"));
        if (!ROLE_SYS_ADMIN.equals(role) && !product.getFarmerId().equals(userId)) {
            throw new BizException(403, "无权查看该产品的农事档案");
        }
        return farmingRecordRepository.findByProductIdOrderByOperationDateDesc(productId);
    }

    /** 面向消费者溯源查询的只读档案列表，无需鉴权。 */
    public List<FarmingRecord> getPublicRecords(Long productId) {
        return farmingRecordRepository.findByProductIdOrderByOperationDateDesc(productId);
    }

    /**
     * 安全间隔期校验：若产品存在用药记录且当前日期仍处于
     * 「操作日期 + 安全间隔天数」内，则禁止生成新溯源码。
     * 供溯源码生成流程调用；命中则抛出 BizException。
     */
    public void assertTraceCodeAllowed(Long productId) {
        LocalDate today = LocalDate.now();
        List<FarmingRecord> pesticides =
                farmingRecordRepository.findByProductIdAndOperationType(productId, OP_PESTICIDE);
        for (FarmingRecord r : pesticides) {
            if (r.getOperationDate() == null || r.getSafetyIntervalDays() == null) {
                continue;
            }
            LocalDate safeUntil = r.getOperationDate().plusDays(r.getSafetyIntervalDays());
            // 处于安全间隔期内(含末日)：今天不晚于安全期末日
            if (!today.isAfter(safeUntil)) {
                throw new BizException(409,
                        "该产品用药「" + r.getDrugName() + "」的安全间隔期至 " + safeUntil
                                + " 止，间隔期内禁止生成溯源码");
            }
        }
    }
}
