import React, { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';

const Acoes = () => {
    const [acoes, setAcoes] = useState([]);
    const [carteiras, setCarteiras] = useState([]);
    const [corretoras, setCorretoras] = useState([]);
    const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });
    const [modalVendaAberto, setModalVendaAberto] = useState(false);

    // Estados default do formulário de cadastro
    const [formData, setFormData] = useState({
        ticker: '',
        mercado: 'BR',
        quantidadeCompra: '1',
        corretoraId: ''
    });
    const [vendaData, setVendaData] = useState({
        acaoIndex: '',
        quantidade: ''
    });
    const [statusMensagem, setStatusMensagem] = useState({ tipo: '', texto: '' });
    const [vendaMensagem, setVendaMensagem] = useState({ tipo: '', texto: '' });

    // Busca os dados no Back-end
    useEffect(() => {
        carregarDados();
    }, []);

    const carregarDados = () => {
        // Busca Ações e Carteiras
        fetch('http://localhost:8080/api/dashboard/acoes')
            .then(response => {
                if (!response.ok) throw new Error("Erro ao carregar dados do servidor");
                return response.json();
            })
            .then(data => {
                setAcoes(data.acoes || []);
                setCarteiras(data.carteiras || []);
            })
            .catch(error => console.error("Erro na requisição das ações:", error));

        // Busca Corretoras
        fetch('http://localhost:8080/corretoras')
            .then(response => response.json())
            .then(data => setCorretoras(data))
            .catch(error => console.error("Erro ao carregar corretoras:", error));
    };

    // Função para lidar com mudanças no formulário
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    // Funcoes da minitela de venda no Front
    const handleVendaInputChange = (e) => {
        const { name, value } = e.target;
        setVendaData({ ...vendaData, [name]: value });
    };

    const abrirModalVenda = () => {
        setVendaMensagem({ tipo: '', texto: '' });
        setVendaData({ acaoIndex: '', quantidade: '' });
        setModalVendaAberto(true);
    };

    const fecharModalVenda = () => {
        setModalVendaAberto(false);
        setVendaData({ acaoIndex: '', quantidade: '' });
    };

    const handleVendaSubmit = (e) => {
        e.preventDefault();
        setVendaMensagem({ tipo: '', texto: '' });

        const acaoSelecionada = acoes[Number(vendaData.acaoIndex)];
        const quantidadeVendida = Number(vendaData.quantidade);
        const quantidadeDisponivel = acaoSelecionada ? Number(acaoSelecionada.quantidadeTotal) : 0;

        if (vendaData.acaoIndex === '' || !acaoSelecionada || !quantidadeVendida || quantidadeVendida <= 0) {
            setVendaMensagem({ tipo: 'error', texto: 'Selecione uma acao e informe uma quantidade valida.' });
            return;
        }

        if (quantidadeVendida > quantidadeDisponivel) {
            setVendaMensagem({ tipo: 'error', texto: `Voce possui apenas ${quantidadeDisponivel} unidade(s) desta acao.` });
            return;
        }

        fetch(`http://localhost:8080/acoes/${encodeURIComponent(acaoSelecionada.ticker)}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ quantidadeVenda: vendaData.quantidade }),
        })
            .then(async response => {
                if (!response.ok) {
                    const errorData = await response.json().catch(() => ({}));
                    throw new Error(errorData.message || "Erro ao vender a acao");
                }
                return response.json();
            })
            .then(() => {
                setVendaMensagem({
                    tipo: 'success',
                    texto: `Venda de ${quantidadeVendida} unidade(s) de ${acaoSelecionada.ticker} registrada com sucesso!`
                });
                setVendaData({ acaoIndex: '', quantidade: '' });
                carregarDados();
            })
            .catch(error => {
                console.error("Erro na venda:", error);
                setVendaMensagem({ tipo: 'error', texto: error.message || 'Erro ao vender acao. Verifique os dados.' });
            });
    };

    // Função para enviar o cadastro ao Back-end
    const handleSubmit = (e) => {
        e.preventDefault();
        setStatusMensagem({ tipo: '', texto: '' });

        // Validação básica no Front
        if (!formData.ticker || !formData.mercado || !formData.quantidadeCompra || !formData.corretoraId) {
            setStatusMensagem({ tipo: 'error', texto: 'Preencha todos os campos obrigatórios.' });
            return;
        }

        const quantidadeCompra = Number(formData.quantidadeCompra);
        if (!quantidadeCompra || quantidadeCompra <= 0) {
            setStatusMensagem({ tipo: 'error', texto: 'Quantidade de compra deve ser maior que zero.' });
            return;
        }

        fetch('http://localhost:8080/acoes', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData),
        })
            .then(response => {
                if (!response.ok) throw new Error("Erro ao cadastrar a ação");
                return response.json();
            })
            .then(() => {
                setStatusMensagem({ tipo: 'success', texto: 'Ação cadastrada com sucesso!' });
                setFormData({ ticker: '', mercado: 'B3', quantidadeCompra: '', corretoraId: '' });
                carregarDados(); // Recarrega a tabela após cadastrar
            })
            .catch(error => {
                console.error("Erro no cadastro:", error);
                setStatusMensagem({ tipo: 'error', texto: 'Erro ao cadastrar ação. Verifique os dados.' });
            });
    };

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

            {/* Minitela de Cadastro */}
            <div className="cadastro-container" style={styles.cadastroContainer}>
                <h3>Cadastrar Nova Ação</h3>
                <form onSubmit={handleSubmit} style={styles.form}>
                    <div style={styles.inputGroup}>
                        <label>Ticker:</label>
                        <input
                            type="text"
                            name="ticker"
                            value={formData.ticker}
                            onChange={handleInputChange}
                            placeholder="Ex: PETR4"
                            style={styles.input}
                        />
                    </div>
                    <div style={styles.inputGroup}>
                        <label>Mercado:</label>
                        <select name="mercado" value={formData.mercado} onChange={handleInputChange} style={styles.input}>
                            <option value="BR">Brasil</option>
                            <option value="US">EUA</option>
                        </select>
                    </div>
                    <div style={styles.inputGroup}>
                        <label>Quantidade:</label>
                        <input
                            type="number"
                            name="quantidadeCompra"
                            value={formData.quantidadeCompra}
                            onChange={handleInputChange}
                            placeholder="Ex: 100"
                            min="0.01"
                            step="0.01"
                            style={styles.input}
                        />
                    </div>
                    <div style={styles.inputGroup}>
                        <label>Corretora:</label>
                        <select name="corretoraId" value={formData.corretoraId} onChange={handleInputChange} style={styles.input}>
                            <option value="">Selecione a corretora...</option>
                            {corretoras.map(corretora => (
                                <option key={corretora.id} value={corretora.id}>
                                    {corretora.nomeFantasia || corretora.razaoSocial}
                                </option>
                            ))}
                        </select>
                    </div>
                    <button type="submit" style={styles.button}>Adicionar</button>
                    <button
                        type="button"
                        onClick={abrirModalVenda}
                        style={{ ...styles.button, ...styles.sellButton }}
                    >
                        Vender
                    </button>
                </form>
                {statusMensagem.texto && (
                    <p style={{ color: statusMensagem.tipo === 'error' ? 'red' : 'green', marginTop: '10px' }}>
                        {statusMensagem.texto}
                    </p>
                )}
            </div>

            {modalVendaAberto && (
                <div style={styles.modalOverlay}>
                    <div style={styles.modal}>
                        <div style={styles.modalHeader}>
                            <h3 style={styles.modalTitle}>Vender Acao</h3>
                            <button
                                type="button"
                                onClick={fecharModalVenda}
                                style={styles.closeButton}
                                aria-label="Fechar venda"
                            >
                                x
                            </button>
                        </div>
                        <form onSubmit={handleVendaSubmit} style={styles.vendaForm}>
                            <div style={styles.inputGroup}>
                                <label>Acao:</label>
                                <select
                                    name="acaoIndex"
                                    value={vendaData.acaoIndex}
                                    onChange={handleVendaInputChange}
                                    style={styles.input}
                                >
                                    <option value="">Selecione a acao...</option>
                                    {acoes.map((acao, index) => (
                                        <option key={`${acao.ticker}-${index}`} value={index}>
                                            {acao.ticker} - {acao.nomeEmpresa || 'Empresa nao informada'} ({acao.quantidadeTotal} disponivel)
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div style={styles.inputGroup}>
                                <label>Quantidade:</label>
                                <input
                                    type="number"
                                    name="quantidade"
                                    value={vendaData.quantidade}
                                    onChange={handleVendaInputChange}
                                    min="0.01"
                                    max={vendaData.acaoIndex !== '' ? acoes[Number(vendaData.acaoIndex)]?.quantidadeTotal : undefined}
                                    step="0.01"
                                    placeholder="Ex: 10"
                                    style={styles.input}
                                />
                            </div>
                            <div style={styles.modalActions}>
                                <button type="button" onClick={fecharModalVenda} style={styles.secondaryButton}>
                                    Cancelar
                                </button>
                                <button type="submit" style={{ ...styles.button, ...styles.sellButton }}>
                                    Confirmar venda
                                </button>
                            </div>
                        </form>
                        {vendaMensagem.texto && (
                            <p style={{
                                color: vendaMensagem.tipo === 'error' ? 'red' : 'green',
                                marginTop: '10px',
                                fontWeight: 600
                            }}>
                                {vendaMensagem.texto}
                            </p>
                        )}
                    </div>
                </div>
            )}

            <div className="table-container">
                {acoes.length > 0 ? (
                    <table id="minhaTabela">
                        <thead>
                        <tr>
                            <th onClick={() => ordenar('ticker')} className="topCollumn">Ticker</th>
                            <th onClick={() => ordenar('corretora')} className="topCollumn">Corretora</th>
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
                                <td>{acao.corretoraRelacionada.nomeFantasia}</td>
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


const styles = {
    cadastroContainer: {
        backgroundColor: '#f9f9f9',
        padding: '20px',
        borderRadius: '8px',
        marginBottom: '20px',
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
    },
    form: {
        display: 'flex',
        gap: '15px',
        alignItems: 'flex-end',
        flexWrap: 'wrap'
    },
    inputGroup: {
        display: 'flex',
        flexDirection: 'column',
        gap: '5px'
    },
    input: {
        padding: '8px',
        borderRadius: '4px',
        border: '1px solid #ccc',
        minWidth: '150px'
    },
    button: {
        padding: '9px 20px',
        backgroundColor: '#007bff',
        color: 'white',
        border: 'none',
        borderRadius: '4px',
        cursor: 'pointer',
        fontWeight: 'bold',
        height: 'fit-content'
    },
    sellButton: {
        backgroundColor: '#dc3545'
    },
    modalOverlay: {
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(15, 23, 42, 0.72)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '20px',
        zIndex: 1000
    },
    modal: {
        width: '100%',
        maxWidth: '420px',
        backgroundColor: '#f9f9f9',
        color: '#111827',
        borderRadius: '8px',
        padding: '20px',
        boxShadow: '0 16px 40px rgba(0,0,0,0.25)'
    },
    modalHeader: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: '12px',
        marginBottom: '16px'
    },
    modalTitle: {
        margin: 0
    },
    closeButton: {
        width: '32px',
        height: '32px',
        border: 'none',
        borderRadius: '50%',
        backgroundColor: '#e5e7eb',
        color: '#111827',
        cursor: 'pointer',
        fontWeight: 'bold'
    },
    vendaForm: {
        display: 'flex',
        flexDirection: 'column',
        gap: '14px'
    },
    modalActions: {
        display: 'flex',
        justifyContent: 'flex-end',
        gap: '10px',
        marginTop: '4px'
    },
    secondaryButton: {
        padding: '9px 16px',
        backgroundColor: '#e5e7eb',
        color: '#111827',
        border: 'none',
        borderRadius: '4px',
        cursor: 'pointer',
        fontWeight: 'bold'
    }
};

export default Acoes;
