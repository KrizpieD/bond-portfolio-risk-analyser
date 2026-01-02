package com.ice.bond_portfolio_risk_analyser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.ice.bond_portfolio_risk_analyser.metrics.service.MetricsService;
import com.ice.bond_portfolio_risk_analyser.metrics.util.MetricType;

class MetricsServiceTest {

    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        metricsService = new MetricsService();
    }

    @Test
    @DisplayName("Should calculate correct YTM for a bond trading at a discount")
    void calculateYTM_DiscountBond() {
        // Setup: A 2-year bond, 4% annual coupon (semi-annual), Face 1000, Price 980
        BigDecimal faceValue = new BigDecimal("1000.00");
        BigDecimal marketPrice = new BigDecimal("980.00");
        BigDecimal couponRate = new BigDecimal("4.00");
        int frequency = 2;

        // Mock dates: Today, +6mo, +12mo, +18mo, +24mo
        LocalDateTime now = LocalDateTime.now();
        List<Timestamp> couponDates = Arrays.asList(
            Timestamp.valueOf(now.plusMonths(6)),
            Timestamp.valueOf(now.plusMonths(12)),
            Timestamp.valueOf(now.plusMonths(18)),
            Timestamp.valueOf(now.plusMonths(24))
        );
        Timestamp maturityDate = Timestamp.valueOf(now.plusMonths(24));

        BigDecimal ytm = metricsService.calculateMetrics(faceValue, marketPrice, couponRate, maturityDate, couponDates, frequency, MetricType.YTM);

        // Expected YTM should be higher than the coupon (4%) because it's a discount bond.
        // Approx 5.06% based on standard financial calculators
        assertThat(ytm.doubleValue()).isCloseTo(0.0506, within(0.001));
    }

    @Test
    @DisplayName("Should calculate Macaulay and Modified Duration correctly")
    void calculateDuration_Comparison() {
        // Inputs for a 5-year bond
        BigDecimal faceValue = new BigDecimal("1000.00");
        BigDecimal marketPrice = new BigDecimal("1000.00"); // Par bond
        BigDecimal couponPayment = new BigDecimal("25.00"); // 5% semi-annual
        double ytm = 0.05;
        double maturityTime = 5.0;
        List<Double> times = Arrays.asList(0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0);

        double macDur = metricsService.calculateMacaulayDuration(faceValue, couponPayment, ytm, times, maturityTime, marketPrice);
        double modDur = metricsService.calculateModifiedDuration(macDur, ytm, 2);

        // For a par bond, Duration is usually slightly less than maturity
        assertThat(macDur).isLessThan(5.0);
        assertThat(macDur).isGreaterThan(4.0);
        
        // Modified duration should be MacDur / (1 + 0.05/2)
        assertThat(modDur).isEqualTo(macDur / 1.025, within(0.0001));
    }

    @Test
    @DisplayName("Should calculate weighted average duration for multiple bonds")
    void calculatePortfolioDuration_WeightedAverage() {
        // Bond 1: 60% of portfolio, Duration 5.0
        MetricsService.BondPosition bond1 = new MetricsService.BondPosition(
            new BigDecimal("60000.00"), 5.0
        );

        // Bond 2: 40% of portfolio, Duration 10.0
        MetricsService.BondPosition bond2 = new MetricsService.BondPosition(
            new BigDecimal("40000.00"), 10.0
        );

        List<MetricsService.BondPosition> portfolio = Arrays.asList(bond1, bond2);

        double result = metricsService.calculatePortfolioDuration(portfolio);

        // (0.6 * 5.0) + (0.4 * 10.0) = 3.0 + 4.0 = 7.0
        assertThat(result).isEqualTo(7.0);
    }
}