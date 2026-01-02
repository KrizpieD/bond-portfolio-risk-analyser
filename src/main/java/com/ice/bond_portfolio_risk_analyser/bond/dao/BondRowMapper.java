package com.ice.bond_portfolio_risk_analyser.bond.dao;

import java.util.Arrays;
import java.util.ArrayList;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.springframework.jdbc.core.RowMapper;

import com.ice.bond_portfolio_risk_analyser.bond.model.Bond;

public class BondRowMapper implements RowMapper<Bond>{
    @Override
    public Bond mapRow(ResultSet rs, int rowNum) throws SQLException {
        Bond bond = new Bond();

        bond.setId(rs.getInt("id"));
        bond.setPortfolioId(rs.getInt("portfolio_id"));
        bond.setIsin(rs.getString("isin"));
        bond.setMaturityDate(rs.getTimestamp("maturity_date"));
        bond.setCouponRate(rs.getDouble("coupon_rate"));
        bond.setFaceValue(rs.getDouble("face_value"));
        bond.setMarketPrice(rs.getDouble("market_price"));

        Array couponDates = rs.getArray("coupon_dates");
        if (couponDates != null) {
            Timestamp[] couponTimestamps = (Timestamp[]) couponDates.getArray();
            bond.setCouponDates(Arrays.asList(couponTimestamps));
        } else {
            bond.setCouponDates(new ArrayList<Timestamp>());
        }


        return bond;
    }    
}
