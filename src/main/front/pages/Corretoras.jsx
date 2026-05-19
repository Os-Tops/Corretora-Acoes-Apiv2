import React, { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';

const Corretoras = () => {
    // Estado para armazenar a lista de corretoras
    const [corretoras, setCorretoras] = useState([]);

    // Simulando a busca de dados do Back-end
    useEffect(() => {
        // Aqui no futuro faremos: fetch('/api/corretoras').then(...)
        // Por enquanto, inicializamos vazio ou com dados de teste
        setCorretoras([]);
    }, []);

    return (
        <div className="container">
            <Navbar title="Corretoras" />

            <div className="table-container">
                {corretoras.length > 0 ? (
                    <table>
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