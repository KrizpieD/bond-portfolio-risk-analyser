package com.ice.bond_portfolio_risk_analyser.bond.model;

import java.sql.Timestamp;

import java.util.List;

import lombok.Data;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Bond {
    private int id;
    private int portfolioId;
    private String isin;
    private Timestamp maturityDate;
    private List<Timestamp> couponDates;
    private double couponRate;
    private double faceValue;
    private double marketPrice;
}
