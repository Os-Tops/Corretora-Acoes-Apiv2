package com.projeto.gestao.service;

import com.projeto.gestao.domain.model.*;
import com.projeto.gestao.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional
public class DBService {

    @Autowired
    private AcaoRepository acaoRepo;

    @Autowired
    private CorretoraRepository corretoraRepo;


    public void initDB() {

        try {


        } catch (Exception e) {
            // Logar o erro ou lançar uma exceção customizada
            System.err.println("Erro ao inicializar o banco de dados: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
