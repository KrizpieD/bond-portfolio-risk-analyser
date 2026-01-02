package com.ice.bond_portfolio_risk_analyser.metrics.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ice.bond_portfolio_risk_analyser.bond.model.Bond;
import com.ice.bond_portfolio_risk_analyser.bond.model.BondPortfolio;
import com.ice.bond_portfolio_risk_analyser.bond.service.BondService;
import com.ice.bond_portfolio_risk_analyser.metrics.util.MetricType;

@Service
public class MetricsService {
    @Autowired
    public BondService bondService;

    /**
     * DTO to hold the results of individual bond calculations
     */
    public static class BondPosition {
        private BigDecimal marketValue; // Market Price * Quantity
        private double modifiedDuration;

        public BondPosition(BigDecimal marketValue, double modifiedDuration) {
            this.marketValue = marketValue;
            this.modifiedDuration = modifiedDuration;
        }
        
        public BigDecimal getMarketValue() { return marketValue; }
        public double getModifiedDuration() { return modifiedDuration; }
    }

    
    private static final int MAX_ITERATIONS = 100;
    private static final BigDecimal PRECISION = new BigDecimal("0.00000001"); // Convergence tolerance
    private static final MathContext MC = MathContext.DECIMAL128; // High precision context

    public BigDecimal getYTMByBondId(int bondId) {
        Bond bond = bondService.getBondById(bondId);

        return calculateMetrics(BigDecimal.valueOf(bond.getFaceValue()),
                            BigDecimal.valueOf(bond.getMarketPrice()),
                            BigDecimal.valueOf(bond.getCouponRate()),
                            bond.getMaturityDate(),
                            bond.getCouponDates(),
                            2, MetricType.YTM); // Using 2 as it is the default frequency for USA, which we are limited to during this assignment.
    }

    public double getDurationByBondId(int bondId) {
        Bond bond = bondService.getBondById(bondId);

        return calculateMetrics(BigDecimal.valueOf(bond.getFaceValue()),
                            BigDecimal.valueOf(bond.getMarketPrice()),
                            BigDecimal.valueOf(bond.getCouponRate()),
                            bond.getMaturityDate(),
                            bond.getCouponDates(),
                            2, MetricType.DURATION).doubleValue();
    }

    public double getModifiedDurationByBondId(int bondId) {
        Bond bond = bondService.getBondById(bondId);

        return calculateMetrics(BigDecimal.valueOf(bond.getFaceValue()),
                            BigDecimal.valueOf(bond.getMarketPrice()),
                            BigDecimal.valueOf(bond.getCouponRate()),
                            bond.getMaturityDate(),
                            bond.getCouponDates(),
                            2, MetricType.MODDEDDURATION).doubleValue();
    }

    public double getPortfolioLevelWeightedAvgDurationByPortfolioId(int portfolioId){
        BondPortfolio bp = bondService.getPortfolio(portfolioId);
        List<BondPosition> positions = bp.getBonds().stream().map(bond -> new BondPosition(BigDecimal.valueOf(bond.getMarketPrice()), getModifiedDurationByBondId(bond.getId()))).collect(Collectors.toList());
        return calculatePortfolioDuration(positions);
    }

    /**
     * Calculates Yield to Maturity (YTM) using the Newton-Raphson method.
     *
     * @param faceValue      The par value (e.g., 1000)
     * @param marketPrice    The current trading price (e.g., 995.50)
     * @param annualCouponRate The annual coupon rate in decimal (e.g., 0.0425 for 4.25%)
     * @param yearsToMaturity The time remaining until maturity in years (e.g., 4.35)
     * @param frequency      Coupons per year (usually 2 for US, 1 for Euro)
     * @return Annualized YTM as a decimal (e.g., 0.045 for 4.5%)
     */
    public BigDecimal calculateMetrics(BigDecimal faceValue, 
                                   BigDecimal marketPrice, 
                                   BigDecimal annualCouponRate, 
                                   Timestamp maturityDate, 
                                   List<Timestamp> couponDates,
                                   int frequency,
                                   MetricType metricType) {
        BigDecimal ytm;

        // Some input validation, as coupon rate isn't stored as the decimal that's expected.
        if(Integer.parseInt(annualCouponRate.toString().substring(0, annualCouponRate.toString().indexOf('.'))) > 0) {
            annualCouponRate = annualCouponRate.divide(BigDecimal.valueOf(100));
        }

        // 1. Convert all future coupon dates to "Time in Years" from today
        LocalDate settlementDate = LocalDate.now();
        List<Double> paymentTimes = couponDates.stream()
                .map(ts -> ts.toLocalDateTime().toLocalDate())
                .filter(date -> date.isAfter(settlementDate)) // Only future flows
                .map(date -> ChronoUnit.DAYS.between(settlementDate, date) / 365.25)
                .collect(Collectors.toList());

        // Add the principal repayment time (Maturity Date)
        double timeToMaturity = ChronoUnit.DAYS.between(settlementDate, maturityDate.toLocalDateTime().toLocalDate()) / 365.25;

        BigDecimal couponPayment = faceValue.multiply(annualCouponRate)
                .divide(BigDecimal.valueOf(frequency), MC);


        // 2. Initial Guess: Simple Current Yield (Annual Coupon / Price)
        // This gives the algorithm a good starting point to converge faster.
        double currentYieldGuess = annualCouponRate.doubleValue(); 
        // Or strictly: (coupon / price) which is also fine.
        
        double y = currentYieldGuess; 

        // 3. Newton-Raphson Iteration
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            
            // Calculate Price at current guess 'y'
            BigDecimal estimatedPrice = calculatePrice(faceValue, couponPayment, y, paymentTimes, timeToMaturity);
            
            // Difference between estimated price and actual market price
            BigDecimal diff = estimatedPrice.subtract(marketPrice);

            // Check if we are close enough (Convergence)
            if (diff.abs().compareTo(PRECISION) < 0) {
                ytm = BigDecimal.valueOf(y).setScale(6, RoundingMode.HALF_UP);
            }

            // Calculate Derivative (Duration-like measure) to find next guess
            BigDecimal derivative = calculatePriceDerivative(faceValue, couponPayment, y, paymentTimes, timeToMaturity);

            // Newton-Raphson Step: y_new = y_old - (f(y) / f'(y))
            // Note: If derivative is 0 (unlikely in bonds), break to avoid divide by zero
            if (derivative.compareTo(BigDecimal.ZERO) == 0) break;
            
            double delta = diff.divide(derivative, MC).doubleValue();
            y = y - delta;
        }

        // If no convergence, assign best estimate ytm or throw error
        ytm = BigDecimal.valueOf(y).setScale(6, RoundingMode.HALF_UP);

        switch(metricType) {
            case DURATION: return BigDecimal.valueOf((double)calculateMacaulayDuration(faceValue,
                            couponPayment, ytm.doubleValue(), paymentTimes, timeToMaturity, marketPrice));
            case MODDEDDURATION: return BigDecimal.valueOf((double) calculateModifiedDuration(calculateMacaulayDuration(faceValue,
                            couponPayment, ytm.doubleValue(), paymentTimes, timeToMaturity, marketPrice),
                            ytm.doubleValue(), 2));
            case YTM:
            default: return ytm;
        }
    }

    /**
     * Calculates price by discounting each specific cash flow date.
     */
    private BigDecimal calculatePrice(BigDecimal faceValue, BigDecimal couponPayment, double annualYield, List<Double> times, double maturityTime) {
        double price = 0.0;

        // Discount each coupon: C / (1 + y)^t
        for (Double t : times) {
            price += couponPayment.doubleValue() / Math.pow(1 + annualYield, t);
        }

        // Discount principal: F / (1 + y)^T
        price += faceValue.doubleValue() / Math.pow(1 + annualYield, maturityTime);

        return BigDecimal.valueOf(price);
    }

    /**
     * Derivative of the price function for Newton-Raphson.
     * f'(y) = Sum [ -t * CF_t * (1 + y)^-(t+1) ]
     */
    private BigDecimal calculatePriceDerivative(BigDecimal faceValue, BigDecimal couponPayment, double annualYield, List<Double> times, double maturityTime) {
        double deriv = 0.0;

        for (Double t : times) {
            deriv += -t * couponPayment.doubleValue() * Math.pow(1 + annualYield, -t - 1);
        }

        deriv += -maturityTime * faceValue.doubleValue() * Math.pow(1 + annualYield, -maturityTime - 1);

        return BigDecimal.valueOf(deriv);
    }

    /**
     * Calculates Macaulay Duration: The weighted average time to receive cash flows.
     * Unit: Years
     */
    public double calculateMacaulayDuration(BigDecimal faceValue, 
                                            BigDecimal couponPayment, 
                                            double ytm, 
                                            List<Double> times, 
                                            double maturityTime, 
                                            BigDecimal marketPrice) {
        double weightedSum = 0.0;

        // Sum of [ t * (CF / (1+y)^t) ]
        for (Double t : times) {
            double pvOfCoupon = couponPayment.doubleValue() / Math.pow(1 + ytm, t);
            weightedSum += t * pvOfCoupon;
        }

        // Add the weighted principal: T * (FaceValue / (1+y)^T)
        double pvOfPrincipal = faceValue.doubleValue() / Math.pow(1 + ytm, maturityTime);
        weightedSum += maturityTime * pvOfPrincipal;

        // Divide by total price to get the weighted average time
        return weightedSum / marketPrice.doubleValue();
    }

    /**
     * Calculates Modified Duration: The % change in price for a 1% change in yield.
     * Unit: Percentage
     */
    public double calculateModifiedDuration(double macDuration, double ytm, int frequency) {
        // Standard Formula: MacDuration / (1 + y/n)
        return macDuration / (1 + (ytm / frequency));
    }

    /**
     * Calculates the Weighted Average Modified Duration for a portfolio.
     * @param positions List of bond positions with their market values and durations
     * @return The portfolio-level modified duration
     */
    public double calculatePortfolioDuration(List<BondPosition> positions) {
        if (positions == null || positions.isEmpty()) {
            return 0.0;
        }

        // 1. Calculate Total Market Value of the Portfolio
        BigDecimal totalPortfolioValue = positions.stream()
                .map(BondPosition::getMarketValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPortfolioValue.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        // 2. Sum the weighted durations
        double weightedDurationSum = 0.0;

        for (BondPosition pos : positions) {
            // Weight = Position Value / Total Portfolio Value
            double weight = pos.getMarketValue()
                    .divide(totalPortfolioValue, 8, RoundingMode.HALF_UP)
                    .doubleValue();

            weightedDurationSum += weight * pos.getModifiedDuration();
        }

        return weightedDurationSum;
    }

}