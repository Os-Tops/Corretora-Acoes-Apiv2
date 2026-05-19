import React, { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';

const Acoes = () => {
    // Estado para guardar os dados vindos do Java
    const [acoes, setAcoes] = useState([]);
    const [carteiras, setCarteiras] = useState([]);
    const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });

    // 1. ETAPA EDUCATIVA: O "fetch" busca os dados no Back-end assim que a página carrega
    useEffect(() => {
        fetch('http://localhost:8080/api/dashboard/acoes')
            .then(response => {
                if (!response.ok) throw new Error("Erro ao carregar dados do servidor");
                return response.json();
            })
            .then(data => {
                // O Java envia um Map com "acoes" e "carteiras"
                setAcoes(data.acoes || []);
                setCarteiras(data.carteiras || []);
            })
            .catch(error => console.error("Erro na requisição:", error));
    }, []);

    // 2. ETAPA EDUCATIVA: Função de ordenação lógica
    const ordenar = (coluna) => {
        let direcao = 'asc';
        if (sortConfig.key === coluna && sortConfig.direction === 'asc') {
            direcao = 'desc';
        }

        const acoesOrdenadas = [...acoes].sort((a, b) => {
            if (a[coluna] < b[coluna]) return direcao === 'asc' ? -1 : 1;
            if (a[coluna] > b[coluna]) return direcao === 'asc' ? 1 : -1;
            return 0;
        });

        setAcoes(acoesOrdenadas);
        setSortConfig({ key: coluna, direction: direcao });
    };

    return (
        <div className="container">
            <Navbar title="Ações Monitoradas" />

            <div className="table-container">
                {acoes.length > 0 ? (
                    <table id="minhaTabela">
                        <thead>
                        <tr>
                            <th onClick={() => ordenar('ticker')} className="topCollumn">Ticker</th>
                            <th onClick={() => ordenar('posicao')} className="topCollumn">Posição</th>
                            <th onClick={() => ordenar('nomeEmpresa')} className="topCollumn">Empresa</th>
                            <th onClick={() => ordenar('mercado')} className="topCollumn">Mercado</th>
                            <th onClick={() => ordenar('quantidadeTotal')} className="topCollumn">Quantidade</th>
                            <th onClick={() => ordenar('cotacaoAtual')} className="topCollumn">Cotação</th>
                            <th onClick={() => ordenar('precoMedio')} className="topCollumn">Preço Médio</th>
                            <th onClick={() => ordenar('dataHoraCotacao')} className="topCollumn">Data Atualização</th>
                        </tr>
                        </thead>
                        <tbody>
                        {acoes.map((acao, index) => (
                            <tr key={index}>
                                <td><span className="ticker-badge">{acao.ticker}</span></td>
                                {/* Formatamos para garantir que exiba BRL ou a moeda vinda do banco */}
                                <td className="price">{`${acao.moeda || 'R$'} ${acao.posicao}`}</td>
                                <td>{acao.nomeEmpresa}</td>
                                <td>{acao.mercado}</td>
                                <td>{acao.quantidadeTotal}</td>
                                <td className="price">{`${acao.moeda || 'R$'} ${acao.cotacaoAtual}`}</td>
                                <td className="price">{`${acao.moeda || 'R$'} ${acao.precoMedio}`}</td>
                                <td>{acao.dataHoraCotacao ? new Date(acao.dataHoraCotacao).toLocaleString('pt-BR') : '---'}</td>
                            </tr>
                        ))}
                        </tbody>
                        {carteiras.length > 0 && (
                            <tfoot>
                            {carteiras.map((carteira, index) => (
                                <tr key={index}>
                                    <td colSpan="7" style={{ textAlign: 'right', fontWeight: 600 }}>
                                        Saldo Total da Carteira:
                                    </td>
                                    <td className="price" style={{ fontWeight: 1000 }}>
                                        {`R$ ${carteira.saldoAcao}`}
                                    </td>
                                </tr>
                            ))}
                            </tfoot>
                        )}
                    </table>
                ) : (
                    <div className="empty-state">
                        <p>Nenhuma ação monitorada no momento.</p>
                        <p>Certifique-se de que o Back-end está rodando e a API retornou dados.</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Acoes;