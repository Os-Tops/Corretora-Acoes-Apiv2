package com.projeto.gestao.infrastructure.client;

import com.projeto.gestao.infrastructure.client.dto.AlphaVantageResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "alphaVantageClient", url = "https://www.alphavantage.co/query")
public interface AlphaVantageClient {

    @GetMapping("?function=GLOBAL_QUOTE")
    AlphaVantageResponseDto consultarCotacao(@RequestParam("symbol") String symbol, @RequestParam("apikey") String apikey);
}
