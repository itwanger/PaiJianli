package com.yizhaoqi.pairesume.controller.admin;

import com.yizhaoqi.pairesume.common.domain.R;
import com.yizhaoqi.pairesume.dto.PriceBaseDTO;
import com.yizhaoqi.pairesume.dto.PricePriorityDTO;
import com.yizhaoqi.pairesume.entity.ReviseBasePrice;
import com.yizhaoqi.pairesume.entity.RevisePriorityPrice;
import com.yizhaoqi.pairesume.service.IPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/revise")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminPriceController {

    private final IPriceService priceService;

    // === 基础价格管理 ===

    @GetMapping("/base-prices")
    public R<List<ReviseBasePrice>> getBasePriceList() {
        return R.ok(priceService.getBasePriceList());
    }

    @PostMapping("/base-prices")
    public R<ReviseBasePrice> createBasePrice(@Valid @RequestBody PriceBaseDTO dto) {
        return R.ok(priceService.createBasePrice(dto));
    }

    @PostMapping("/base-prices/{id}")
    public R<ReviseBasePrice> updateBasePrice(@PathVariable Long id, @Valid @RequestBody PriceBaseDTO dto) {
        return R.ok(priceService.updateBasePrice(id, dto));
    }

    // === 加塞价格管理 ===

    @GetMapping("/priority-prices")
    public R<List<RevisePriorityPrice>> getPriorityPriceList() {
        return R.ok(priceService.getPriorityPriceList());
    }

    @PostMapping("/priority-prices")
    public R<RevisePriorityPrice> createPriorityPrice(@Valid @RequestBody PricePriorityDTO dto) {
        return R.ok(priceService.createPriorityPrice(dto));
    }

    @PostMapping("/priority-prices/{id}")
    public R<RevisePriorityPrice> updatePriorityPrice(@PathVariable Long id, @Valid @RequestBody PricePriorityDTO dto) {
        return R.ok(priceService.updatePriorityPrice(id, dto));
    }
}
