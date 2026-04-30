package com.projeto.gestao.service;

import com.projeto.gestao.domain.model.Corretora;
import com.projeto.gestao.domain.port.BuscaCepPort;
import com.projeto.gestao.domain.port.ValidacaoCnpjPort;
import com.projeto.gestao.repository.CorretoraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CorretoraService {

    private final CorretoraRepository corretoraRepository;
    private final ValidacaoCnpjPort validacaoCnpjPort;
    private final BuscaCepPort buscaCepPort;

    public CorretoraService(CorretoraRepository corretoraRepository, ValidacaoCnpjPort validacaoCnpjPort, BuscaCepPort buscaCepPort) {
        this.corretoraRepository = corretoraRepository;
        this.validacaoCnpjPort = validacaoCnpjPort;
        this.buscaCepPort = buscaCepPort;
    }

    @Transactional
    public Corretora cadastrarCorretora(String cnpj) {
        // Remover pontuacao do CNPJ
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");
        
        if (corretoraRepository.existsByCnpj(cnpjLimpo)) {
            throw new IllegalArgumentException("Corretora com este CNPJ já existe.");
        }

        ValidacaoCnpjPort.CnpjInfo cnpjInfo = validacaoCnpjPort.getCnpjInfo(cnpjLimpo);
        if (cnpjInfo == null) {
            throw new IllegalArgumentException("Não foi possível obter dados para este CNPJ na Brasil API.");
        }

        boolean validadaCvm = validacaoCnpjPort.isValidAndMarketParticipant(cnpjLimpo);

        Corretora corretora = new Corretora();
        corretora.setCnpj(cnpjLimpo);
        corretora.setRazaoSocial(cnpjInfo.razaoSocial());
        corretora.setNomeFantasia(cnpjInfo.nomeFantasia());
        corretora.setEmail(cnpjInfo.email());
        corretora.setTelefone(cnpjInfo.telefone());
        corretora.setSituacaoCadastral(cnpjInfo.situacaoCadastral());
        corretora.setValidadaNaCvm(validadaCvm);
        corretora.setDataCadastro(LocalDateTime.now());

        if (cnpjInfo.cep() != null && !cnpjInfo.cep().isEmpty()) {
            BuscaCepPort.CepInfo cepInfo = buscaCepPort.getCepInfo(cnpjInfo.cep().replaceAll("[^0-9]", ""));
            if (cepInfo != null) {
                corretora.setCep(cepInfo.cep());
                corretora.setLogradouro(cepInfo.logradouro());
                corretora.setBairro(cepInfo.bairro());
                corretora.setCidade(cepInfo.localidade());
                corretora.setUf(cepInfo.uf());
                corretora.setComplemento(cepInfo.complemento());
            } else {
                corretora.setCep(cnpjInfo.cep());
                corretora.setLogradouro(cnpjInfo.logradouro());
                corretora.setBairro(cnpjInfo.bairro());
                corretora.setCidade(cnpjInfo.municipio());
                corretora.setUf(cnpjInfo.uf());
                corretora.setNumero(cnpjInfo.numero());
                corretora.setComplemento(cnpjInfo.complemento());
            }
        }

        return corretoraRepository.save(corretora);
    }

    public List<Corretora> listarTodas() {
        return corretoraRepository.findAll();
    }

    public Optional<Corretora> buscarPorId(UUID id) {
        return corretoraRepository.findById(id);
    }

    public Optional<Corretora> buscarPorCnpj(String cnpj) {
        return corretoraRepository.findByCnpj(cnpj.replaceAll("[^0-9]", ""));
    }
}
