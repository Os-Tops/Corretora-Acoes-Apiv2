package com.projeto.gestao.infrastructure.adapter;

import com.projeto.gestao.domain.port.ValidacaoCnpjPort;
import com.projeto.gestao.infrastructure.client.BrasilApiClient;
import com.projeto.gestao.infrastructure.client.dto.BrasilApiCnpjDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class BrasilApiAdapter implements ValidacaoCnpjPort {

    private final BrasilApiClient brasilApiClient;

    public BrasilApiAdapter(BrasilApiClient brasilApiClient) {
        this.brasilApiClient = brasilApiClient;
    }

    @Override
    public boolean isValidAndMarketParticipant(String cnpj) {
        try {
            BrasilApiCnpjDto dto = brasilApiClient.consultarCnpj(cnpj);
            if (dto == null) return false;

            // 2 = ATIVA
            if (dto.getSituacaoCadastral() != 2) {
                return false;
            }

            // CNAEs financeiros: 64xx (bancos/crédito) e 66xx (aux. financeiros/corretoras)
            if (dto.getCnae_principal() != null && dto.getCnae_principal().getCodigo() != null) {
                String cnae = String.valueOf(dto.getCnae_principal().getCodigo());
                return cnae.startsWith("64") || cnae.startsWith("66");
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Cacheable(value = "cnpj", key = "#cnpj")
    public CnpjInfo getCnpjInfo(String cnpj) {
        BrasilApiCnpjDto dto = brasilApiClient.consultarCnpj(cnpj);
        if (dto == null) return null;

        String cnaeDescricao = dto.getCnae_principal() != null ? dto.getCnae_principal().getDescricao() : "";
        return new CnpjInfo(
            dto.getRazaoSocial(),
            dto.getNomeFantasia(),
            cnaeDescricao,
            dto.getCep(),
            dto.getEmail(),
            dto.getDdd_telefone_1(),
            dto.getLogradouro(),
            dto.getNumero(),
            dto.getComplemento(),
            dto.getBairro(),
            dto.getMunicipio(),
            dto.getUf(),
            dto.getDescricaoSituacaoCadastral()
        );
    }
}
