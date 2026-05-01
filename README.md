# 📌 API de Gestão de Ações e Corretoras

Este projeto consiste em uma **API RESTful** desenvolvida para gerenciar corretoras de valores e ativos financeiros (ações). A API integra-se com diversas fontes externas para obter dados financeiros e validar informações de mercado, enriquecendo os cadastros locais e garantindo a integridade das informações financeiras.

## 🎯 Objetivo
Integrar as **APIs externas** essenciais para o sistema de gestão de ações e corretoras, garantindo que as informações de mercado sejam precisas e validadas.

---

## ✅ APIs Utilizadas

### 1. **[brapi.dev](https://brapi.dev)** - Ações Brasileiras
- **Descrição**: Fornece informações sobre ações da bolsa brasileira, incluindo cotações atualizadas.
- **Uso no Sistema**: Consultada para obter a cotação das ações brasileiras, com mercado "BR", garantindo dados atualizados e precisos sobre os ativos da bolsa de valores brasileira.

### 2. **[alphavantage.co](https://www.alphavantage.co)** - Ações Bolsa Americana
- **Descrição**: Oferece dados sobre as cotações de ações da bolsa dos Estados Unidos, com informações detalhadas sobre diversos ativos.
- **Uso no Sistema**: Utilizada para consultar cotações de ações nos mercados dos EUA, permitindo acesso a dados financeiros internacionais.

### 3. **[Brasil API](https://brasilapi.com.br)** - Diversas APIs para Dados Brasileiros
- **Descrição**: API que oferece acesso a vários dados sobre o Brasil, como CNPJ, dados de empresas e endereços de locais.
- **Uso no Sistema**: Consultada para obter informações detalhadas sobre o CNPJ das corretoras, validando as instituições financeiras registradas no Brasil.

### 4. **[ViaCEP](https://viacep.com.br)** - CEP das Cidades Brasileiras
- **Descrição**: API que consulta o CEP de cidades brasileiras, fornecendo informações de localização.
- **Uso no Sistema**: Utilizada para buscar e validar os endereços das corretoras, garantindo a precisão das informações de localização (como logradouro, número, bairro, cidade e estado).

---

## 🎯 Como Funciona
- **CNPJ e Instituições**: Utiliza a **Brasil API** para obter e validar dados relacionados ao CNPJ das corretoras.
- **Endereço (CEP)**: A consulta ao CEP das corretoras é feita via **ViaCEP** para garantir que as informações de endereço estejam corretas.
- **Cotação de Ações**: 
  - **Ações Brasileiras**: Consultada via **brapi.dev** para obter cotações de ações da bolsa brasileira.
  - **Ações Americanas**: Consultadas via **Alpha Vantage** para obter cotações de ações da bolsa americana.

---

## ✅ Tecnologias e Ferramentas
- **Linguagem/Framework**: Java com Spring Boot.
- **Banco de Dados**: H2 (para testes) e PostgreSQL (para produção).
- **Comunicação Externa**: Usamos Spring Cloud OpenFeign ou WebClient para consumo das APIs externas.
