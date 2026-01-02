package com.ice.bond_portfolio_risk_analyser.bond.model;

import java.util.List;

import lombok.Data;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BondPortfolio {
    private int id;
    private String portfolioName;
    private List<Bond> bonds;
}
