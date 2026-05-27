import React, { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';

const Corretoras = () => {
    // Estado para guardar os dados vindos do Java
    const [corretoras, setCorretoras] = useState([]);
    const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });

    // 1. ETAPA EDUCATIVA: O "fetch" busca os dados no Back-end assim que a página carrega
    useEffect(() => {
        fetch('http://localhost:8080/api/dashboard/corretoras')
            .then(response => {
                if (!response.ok) throw new Error("Erro ao carregar dados do servidor");
                return response.json();
            })
            .then(data => {
                // Vamos olhar no console do navegador o que o Java realmente mandou
                console.log("Dados recebidos da API de corretoras:", data);

                // Se 'data' já for um Array direto, salvamos ele.
                // Se for um Objeto (Map), procuramos a chave 'corretoras'.
                if (Array.isArray(data)) {
                    setCorretoras(data);
                } else {
                    setCorretoras(data.corretoras || []);
                }
            })
            .catch(error => console.error("Erro na requisição:", error));
    }, []);

    // 2. ETAPA EDUCATIVA: Função de ordenação lógica
    const ordenar = (coluna) => {
        let direcao = 'asc';
        if (sortConfig.key === coluna && sortConfig.direction === 'asc') {
            direcao = 'desc';
        }

        const corretorasOrdenadas = [...corretoras].sort((a, b) => {
            if (a[coluna] < b[coluna]) return direcao === 'asc' ? -1 : 1;
            if (a[coluna] > b[coluna]) return direcao === 'asc' ? 1 : -1;
            return 0;
        });

        setCorretoras(corretorasOrdenadas);
        setSortConfig({ key: coluna, direction: direcao });
    };

    return (
        <div className="container">
            <Navbar title="Corretoras" />

            <div className="table-container">
                {corretoras.length > 0 ? (
                    <table id="minhaTabela">
                        <thead>
                        <tr>
                            <th>CNPJ</th>
                            <th>Razão Social</th>
                            <th>Telefone</th>
                            <th>Cidade/UF</th>
                            <th>Validação CVM</th>
                        </tr>
                        </thead>
                        <tbody>
                        {corretoras.map((corretora, index) => (
                            <tr key={index}>
                                <td>{corretora.cnpj}</td>
                                <td>{corretora.razaoSocial}</td>
                                <td>{corretora.telefone}</td>
                                <td>{`${corretora.cidade}/${corretora.uf}`}</td>
                                <td>
                                    {corretora.validadaNaCvm ? (
                                        <span className="badge success">Validada</span>
                                    ) : (
                                        <span className="badge warning">Pendente</span>
                                    )}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                ) : (
                    <div className="empty-state">
                        <p>Nenhuma corretora cadastrada no momento.</p>
                        <p>Utilize a API POST /corretoras para cadastrar via CNPJ.</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Corretoras;