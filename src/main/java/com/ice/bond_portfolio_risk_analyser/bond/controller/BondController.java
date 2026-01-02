package com.ice.bond_portfolio_risk_analyser.bond.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ice.bond_portfolio_risk_analyser.bond.model.Bond;
import com.ice.bond_portfolio_risk_analyser.bond.model.BondPortfolio;
import com.ice.bond_portfolio_risk_analyser.bond.service.BondService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;

@RestController
@RequestMapping("/bond")
public class BondController {
    @Autowired
    public BondService bondService;

    @PostMapping("/create-bond")
    public void createBond(@RequestBody String json) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(Bond.class);
        Bond requestedBond = reader.readValue(json);
        
        bondService.createBond(requestedBond);
    }

    @PostMapping("/create-portfolio")
    public int createPortfolio(@RequestBody String json) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectReader reader = mapper.readerFor(BondPortfolio.class);
        BondPortfolio requestedBondPortfolio = reader.readValue(json);
        
        return bondService.createPortfolio(requestedBondPortfolio);
    }

    @GetMapping("/get-portfolio/{id}")
    public BondPortfolio getPortfolio(@PathVariable int id) {
        return bondService.getPortfolio(id);
    }

}
