package com.yizhaoqi.pairesume.service;

import com.yizhaoqi.pairesume.dto.PriceBaseDTO;
import com.yizhaoqi.pairesume.dto.PricePriorityDTO;
import com.yizhaoqi.pairesume.entity.ReviseBasePrice;
import com.yizhaoqi.pairesume.entity.RevisePriorityPrice;

import java.util.List;

public interface IPriceService {

    // === 基础价格管理 ===
    List<ReviseBasePrice> getBasePriceList();
    ReviseBasePrice createBasePrice(PriceBaseDTO dto);
    ReviseBasePrice updateBasePrice(Long id, PriceBaseDTO dto);

    // === 加塞价格管理 ===
    List<RevisePriorityPrice> getPriorityPriceList();
    RevisePriorityPrice createPriorityPrice(PricePriorityDTO dto);
    RevisePriorityPrice updatePriorityPrice(Long id, PricePriorityDTO dto);
}
