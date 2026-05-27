import React, { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';

const Corretoras = () => {
    // Estados para a tabela de corretoras
    const [corretoras, setCorretoras] = useState([]);
    const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });

    // 1. NOVOS ESTADOS: Para o formulário de adicionar CNPJ
    const [cnpjInput, setCnpjInput] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [mensagem, setMensagem] = useState({ texto: '', tipo: '' }); // tipo: 'success' ou 'error'

    // Função de carregamento inicial
    useEffect(() => {
        fetch('http://localhost:8080/api/dashboard/corretoras')
            .then(response => {
                if (!response.ok) throw new Error("Erro ao carregar dados do servidor");
                return response.json();
            })
            .then(data => {
                if (Array.isArray(data)) {
                    setCorretoras(data);
                } else {
                    setCorretoras(data.corretoras || []);
                }
            })
            .catch(error => console.error("Erro na requisição GET:", error));
    }, []);

    // 2. NOVA FUNÇÃO: Cadastrar corretora via POST
    const handleAdicionarCorretora = (e) => {
        e.preventDefault(); // Evita que a página recarregue ao enviar o formulário
        if (!cnpjInput.trim()) return;

        setIsLoading(true);
        setMensagem({ texto: '', tipo: '' }); // Reseta a mensagem

        // O seu controller Spring mapeia o POST para /corretoras
        fetch('http://localhost:8080/corretoras', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ cnpj: cnpjInput }) // Envia o Map<String, String> esperado pelo Java
        })
            .then(async response => {
                if (!response.ok) {
                    // Tenta capturar a mensagem de erro que o Java (IllegalArgumentException) devolve
                    const errorText = await response.text();
                    throw new Error(errorText || "Erro ao adicionar corretora.");
                }
                return response.json();
            })
            .then(novaCorretora => {
                // Adiciona a nova corretora à lista existente sem precisar recarregar a página
                setCorretoras(prev => [...prev, novaCorretora]);
                setCnpjInput(''); // Limpa o campo
                setMensagem({ texto: 'Corretora adicionada com sucesso!', tipo: 'success' });
            })
            .catch(error => {
                console.error("Erro no POST:", error);
                setMensagem({ texto: error.message, tipo: 'error' });
            })
            .finally(() => {
                setIsLoading(false);
                // Limpa a mensagem após 4 segundos
                setTimeout(() => setMensagem({ texto: '', tipo: '' }), 4000);
            });
    };

    // Função de ordenação
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

                {/* 3. BARRA DE ADIÇÃO DE CORRETORA */}
                <div className="add-corretora-bar" style={{ marginBottom: '20px', padding: '15px', backgroundColor: '#f9f9f9', borderRadius: '8px' }}>
                    <form onSubmit={handleAdicionarCorretora} style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                        <label htmlFor="cnpj-input"><strong>Adicionar por CNPJ:</strong></label>
                        <input
                            id="cnpj-input"
                            type="text"
                            placeholder="Apenas números ou formatado..."
                            value={cnpjInput}
                            onChange={(e) => setCnpjInput(e.target.value)}
                            disabled={isLoading}
                            style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc', flex: '1', maxWidth: '300px' }}
                        />
                        <button
                            type="submit"
                            disabled={isLoading || !cnpjInput}
                            style={{ padding: '8px 16px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                        >
                            {isLoading ? 'Buscando...' : 'Adicionar'}
                        </button>
                    </form>

                    {/* Exibição de Mensagens de Sucesso ou Erro */}
                    {mensagem.texto && (
                        <div style={{
                            marginTop: '10px',
                            color: mensagem.tipo === 'error' ? 'red' : 'green',
                            fontWeight: 'bold'
                        }}>
                            {mensagem.texto}
                        </div>
                    )}
                </div>
                {/* FIM DA BARRA DE ADIÇÃO */}

                {corretoras.length > 0 ? (
                    <table id="minhaTabela">
                        <thead>
                        <tr>
                            <th onClick={() => ordenar('cnpj')} style={{cursor: 'pointer'}}>CNPJ</th>
                            <th onClick={() => ordenar('razaoSocial')} style={{cursor: 'pointer'}}>Razão Social</th>
                            <th>Telefone</th>
                            <th>Cidade/UF</th>
                            <th onClick={() => ordenar('validadaNaCvm')} style={{cursor: 'pointer'}}>Validação CVM</th>
                        </tr>
                        </thead>
                        <tbody>
                        {corretoras.map((corretora, index) => (
                            <tr key={index}>
                                <td>{corretora.cnpj}</td>
                                <td>{corretora.razaoSocial}</td>
                                <td>{corretora.telefone || '---'}</td>
                                <td>{`${corretora.cidade || '---'}/${corretora.uf || '---'}`}</td>
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
                        <p>Utilize a barra acima para cadastrar via CNPJ.</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Corretoras;