package com.projeto.gestao.infra.client.dto;

import java.math.BigDecimal;
import java.util.List;

public class BrapiResponseDto {
    private List<BrapiResultDto> results;

    public List<BrapiResultDto> getResults() { return results; }
    public void setResults(List<BrapiResultDto> results) { this.results = results; }

    public static class BrapiResultDto {
        private String symbol;
        private String shortName;
        private String currency;
        private BigDecimal regularMarketPrice;

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        
        public String getShortName() { return shortName; }
        public void setShortName(String shortName) { this.shortName = shortName; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        
        public BigDecimal getRegularMarketPrice() { return regularMarketPrice; }
        public void setRegularMarketPrice(BigDecimal regularMarketPrice) { this.regularMarketPrice = regularMarketPrice; }
    }
}
