package com.yizhaoqi.pairesume.service.impl;

import com.yizhaoqi.pairesume.common.exception.BusinessException;
import com.yizhaoqi.pairesume.dto.PriceBaseDTO;
import com.yizhaoqi.pairesume.dto.PricePriorityDTO;
import com.yizhaoqi.pairesume.entity.ReviseBasePrice;
import com.yizhaoqi.pairesume.entity.RevisePriorityPrice;
import com.yizhaoqi.pairesume.repository.ReviseBasePriceRepository;
import com.yizhaoqi.pairesume.repository.RevisePriorityPriceRepository;
import com.yizhaoqi.pairesume.service.IPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceServiceImpl implements IPriceService {

    private final ReviseBasePriceRepository basePriceRepository;
    private final RevisePriorityPriceRepository priorityPriceRepository;

    // === 基础价格管理 ===

    @Override
    public List<ReviseBasePrice> getBasePriceList() {
        return basePriceRepository.findAll();
    }

    @Override
    public ReviseBasePrice createBasePrice(PriceBaseDTO dto) {
        basePriceRepository.findByUsageCount(dto.getUsageCount()).ifPresent(p -> {
            throw new BusinessException("该使用次数的价格规则已存在");
        });
        ReviseBasePrice price = new ReviseBasePrice();
        price.setUsageCount(dto.getUsageCount());
        price.setAmount(dto.getAmount());
        price.setDescription(dto.getDescription());
        price.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        return basePriceRepository.save(price);
    }

    @Override
    public ReviseBasePrice updateBasePrice(Long id, PriceBaseDTO dto) {
        ReviseBasePrice price = basePriceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("未找到ID为 " + id + " 的基础价格规则"));
        
        // 如果修改了 usageCount，需要检查是否与其他规则冲突
        if (dto.getUsageCount() != null && !dto.getUsageCount().equals(price.getUsageCount())) {
            basePriceRepository.findByUsageCount(dto.getUsageCount()).ifPresent(p -> {
                throw new BusinessException("该使用次数的价格规则已存在");
            });
            price.setUsageCount(dto.getUsageCount());
        }

        if (dto.getAmount() != null) price.setAmount(dto.getAmount());
        if (dto.getDescription() != null) price.setDescription(dto.getDescription());
        if (dto.getStatus() != null) price.setStatus(dto.getStatus());
        
        return basePriceRepository.save(price);
    }

    // === 加塞价格管理 ===

    @Override
    public List<RevisePriorityPrice> getPriorityPriceList() {
        return priorityPriceRepository.findAll();
    }

    @Override
    public RevisePriorityPrice createPriorityPrice(PricePriorityDTO dto) {
        priorityPriceRepository.findByPriorityLevel(dto.getPriorityLevel()).ifPresent(p -> {
            throw new BusinessException("该加塞等级的价格规则已存在");
        });
        RevisePriorityPrice price = new RevisePriorityPrice();
        price.setPriorityLevel(dto.getPriorityLevel());
        price.setAmount(dto.getAmount());
        price.setDescription(dto.getDescription());
        price.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        return priorityPriceRepository.save(price);
    }

    @Override
    public RevisePriorityPrice updatePriorityPrice(Long id, PricePriorityDTO dto) {
        RevisePriorityPrice price = priorityPriceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("未找到ID为 " + id + " 的加塞价格规则"));

        // 如果修改了 priorityLevel，需要检查是否与其他规则冲突
        if (dto.getPriorityLevel() != null && !dto.getPriorityLevel().equals(price.getPriorityLevel())) {
            priorityPriceRepository.findByPriorityLevel(dto.getPriorityLevel()).ifPresent(p -> {
                throw new BusinessException("该加塞等级的价格规则已存在");
            });
            price.setPriorityLevel(dto.getPriorityLevel());
        }

        if (dto.getAmount() != null) price.setAmount(dto.getAmount());
        if (dto.getDescription() != null) price.setDescription(dto.getDescription());
        if (dto.getStatus() != null) price.setStatus(dto.getStatus());

        return priorityPriceRepository.save(price);
    }
}
