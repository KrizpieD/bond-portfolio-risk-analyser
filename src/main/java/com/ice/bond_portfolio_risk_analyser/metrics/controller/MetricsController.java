package com.ice.bond_portfolio_risk_analyser.metrics.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

import com.ice.bond_portfolio_risk_analyser.metrics.service.MetricsService;

@RestController
@RequestMapping("/metrics")
public class MetricsController {
    @Autowired
    public MetricsService metricsService;

    @GetMapping("/get-ytm-by-bond-id/{id}")
    public BigDecimal getYTMByBondId(@PathVariable int id) {
        return metricsService.getYTMByBondId(id);
    }

    @GetMapping("/get-duration-by-bond-id/{id}")
    public double getDurationByBondId(@PathVariable int id) {
        return metricsService.getDurationByBondId(id);
    }

    @GetMapping("/get-modded-duration-by-bond-id/{id}")
    public double getModifiedDurationByBondId(@PathVariable int id) {
        return metricsService.getModifiedDurationByBondId(id);
    }

    @GetMapping("/get-portfolio-weighted-avg-duration/{id}")
    public double getPortfolioWeightedAvgDuration(@PathVariable int id) {
        return metricsService.getPortfolioLevelWeightedAvgDurationByPortfolioId(id);
    }

}
