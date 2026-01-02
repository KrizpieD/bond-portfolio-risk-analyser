package com.ice.bond_portfolio_risk_analyser.bond.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.ice.bond_portfolio_risk_analyser.bond.dao.BondRowMapper;
import com.ice.bond_portfolio_risk_analyser.bond.model.Bond;
import com.ice.bond_portfolio_risk_analyser.bond.model.BondPortfolio;

@Service
public class BondService {
    @Autowired
    public JdbcTemplate jdbcTemplate;

    public void createBond(Bond bond) {
        jdbcTemplate.update("""
            insert into bpra.bond (
                portfolio_id,
                isin,
                maturity_date,
                coupon_rate,
                face_value,
                market_price
                ) values (?,?,?,?,?,?)
            """,
            bond.getPortfolioId(),
            bond.getIsin(),
            bond.getMaturityDate(),
            bond.getCouponRate(),
            bond.getFaceValue(),
            bond.getMarketPrice()
        );
        // Newly inserted bond will always have the highest ID number. We will grab that now to insert the coupon_dates.
        int bondId = jdbcTemplate.queryForObject("select max(id) from bpra.bond", Integer.class);

        for(Timestamp couponDate : bond.getCouponDates()) {
            jdbcTemplate.update("insert into bpra.bond_coupon_date (bond_id, coupon_date) values (?,?)", bondId, couponDate);
        }
    }

    public int createPortfolio(BondPortfolio portfolio) {
        jdbcTemplate.update("insert into bpra.bond_portfolio (portfolio_name) values(?)", portfolio.getPortfolioName());
        
        // Newly inserted portfolio will always have the highest ID number. We will grab that now to insert the bonds.
        int portfolioId = jdbcTemplate.queryForObject("select max(id) from bpra.bond_portfolio", Integer.class);
        
        for(Bond bond : portfolio.getBonds()) {
            bond.setPortfolioId(portfolioId);
            createBond(bond);
        }

        return portfolioId;
    }

    public BondPortfolio getPortfolio(int id) {
        BondPortfolio bp = new BondPortfolio();
        bp.setId(id);
        bp.setPortfolioName(jdbcTemplate.queryForObject(
            "select portfolio_name from bpra.bond_portfolio where id = ?", String.class, id));
        bp.setBonds(getBondsByPortfolioId(id));
        return bp;
    }

    public List<Bond> getBondsByPortfolioId(int portfolioId) {
        return jdbcTemplate.query("select b.*, array_agg(bcd.coupon_date) as coupon_dates from bpra.bond b " +
        "left join bpra.bond_coupon_date bcd on b.id = bcd.bond_id " +
        "where b.portfolio_id = ? group by b.id",
         new BondRowMapper(), portfolioId);
    }

    public Bond getBondById(int bondId) {
        return jdbcTemplate.queryForObject("select b.*, array_agg(bcd.coupon_date) as coupon_dates from bpra.bond b " +
        "left join bpra.bond_coupon_date bcd on b.id = bcd.bond_id " +
        "where b.id = ? group by b.id",
        new BondRowMapper(), bondId);
    }
}
