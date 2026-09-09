import React, { useEffect, useMemo, useState } from 'react';
import Navbar from '../components/Navbar';
import { apiFetch } from '../services/api';
import { hasRole } from '../services/auth';
import { getNomeCorretora } from '../services/corretoras';

const formatCurrency = (value, currency = 'BRL') => Number(value || 0).toLocaleString('pt-BR', {
    style: 'currency',
    currency: currency === 'USD' ? 'USD' : 'BRL'
});

const today = () => new Date().toISOString().slice(0, 10);

const tiposMovimentacao = [
    ['APORTE', 'Aporte'],
    ['RETIRADA', 'Retirada'],
    ['COMPRA', 'Compra'],
    ['VENDA', 'Venda'],
    ['DIVIDENDO', 'Dividendo'],
    ['JCP', 'JCP'],
    ['TAXA', 'Taxa'],
    ['IMPOSTO', 'Imposto']
];

const tiposComAtivo = new Set(['COMPRA', 'VENDA']);
const tiposComAtivoOpcional = new Set(['DIVIDENDO', 'JCP']);

const mensagemErro = async (response, fallback) => {
    const data = await response.json().catch(() => ({}));
    return data.message || fallback;
};

const novaMovimentacao = () => ({
    tipo: 'APORTE',
    acaoId: '',
    contaCorretoraId: '',
    dataOperacao: today(),
    quantidade: '',
    precoUnitario: '',
    valor: '',
    taxas: '',
    impostos: '',
    observacao: ''
});

const Carteiras = () => {
    const isInvestor = hasRole('USER');
    const [carteiras, setCarteiras] = useState([]);
    const [carteiraId, setCarteiraId] = useState('');
    const [carteira, setCarteira] = useState(null);
    const [posicoes, setPosicoes] = useState([]);
    const [movimentacoes, setMovimentacoes] = useState([]);
    const [contas, setContas] = useState([]);
    const [catalogo, setCatalogo] = useState([]);
    const [corretoras, setCorretoras] = useState([]);
    const [nomeCarteira, setNomeCarteira] = useState('');
    const [contaForm, setContaForm] = useState({ corretoraId: '', apelido: '', identificadorConta: '' });
    const [movimentacao, setMovimentacao] = useState(novaMovimentacao);
    const [mensagem, setMensagem] = useState({ tipo: '', texto: '' });
    const [isLoading, setIsLoading] = useState(false);

    const carregarCarteiras = async (preferida) => {
        const response = await apiFetch('/carteiras');
        if (!response.ok) {
            throw new Error(await mensagemErro(response, 'Não foi possível carregar as carteiras.'));
        }
        const data = await response.json();
        setCarteiras(data);
        const proxima = preferida || carteiraId || data.find((item) => item.principal)?.id || data[0]?.id || '';
        setCarteiraId(proxima ? String(proxima) : '');
    };

    const carregarDadosCarteira = async () => {
        if (!carteiraId) {
            setCarteira(null);
            setPosicoes([]);
            setMovimentacoes([]);
            setContas([]);
            return;
        }

        setIsLoading(true);
        try {
            const [carteiraResponse, posicoesResponse, movimentacoesResponse, contasResponse] = await Promise.all([
                apiFetch(`/carteiras/${carteiraId}`),
                apiFetch(`/carteiras/${carteiraId}/posicoes`),
                apiFetch(`/carteiras/${carteiraId}/movimentacoes`),
                apiFetch(`/carteiras/${carteiraId}/contas`)
            ]);
            const responses = [carteiraResponse, posicoesResponse, movimentacoesResponse, contasResponse];
            if (responses.some((response) => !response.ok)) {
                const respostaComErro = responses.find((response) => !response.ok);
                throw new Error(await mensagemErro(respostaComErro, 'Não foi possível carregar os dados da carteira.'));
            }
            setCarteira(await carteiraResponse.json());
            setPosicoes(await posicoesResponse.json());
            setMovimentacoes(await movimentacoesResponse.json());
            setContas(await contasResponse.json());
        } catch (error) {
            setMensagem({ tipo: 'error', texto: error.message });
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        if (!isInvestor) {
            return;
        }
        const carregarBase = async () => {
            try {
                const [catalogoResponse, corretorasResponse] = await Promise.all([
                    apiFetch('/acoes'),
                    apiFetch('/corretoras')
                ]);
                if (!catalogoResponse.ok || !corretorasResponse.ok) {
                    throw new Error('Não foi possível carregar os dados para lançamento.');
                }
                setCatalogo(await catalogoResponse.json());
                setCorretoras((await corretorasResponse.json()).filter((item) => item.validadaNaCvm === true));
                await carregarCarteiras();
            } catch (error) {
                setMensagem({ tipo: 'error', texto: error.message });
            }
        };
        carregarBase();
    }, [isInvestor]);

    useEffect(() => {
        if (isInvestor) {
            carregarDadosCarteira();
        }
    }, [carteiraId]);

    const criarCarteira = async (event) => {
        event.preventDefault();
        setMensagem({ tipo: '', texto: '' });
        try {
            const response = await apiFetch('/carteiras', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ nome: nomeCarteira })
            });
            if (!response.ok) {
                throw new Error(await mensagemErro(response, 'Não foi possível criar a carteira.'));
            }
            const criada = await response.json();
            setNomeCarteira('');
            await carregarCarteiras(criada.id);
            setMensagem({ tipo: 'success', texto: 'Carteira criada com sucesso.' });
        } catch (error) {
            setMensagem({ tipo: 'error', texto: error.message });
        }
    };

    const criarConta = async (event) => {
        event.preventDefault();
        setMensagem({ tipo: '', texto: '' });
        try {
            const response = await apiFetch(`/carteiras/${carteiraId}/contas`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(contaForm)
            });
            if (!response.ok) {
                throw new Error(await mensagemErro(response, 'Não foi possível vincular a corretora.'));
            }
            setContaForm({ corretoraId: '', apelido: '', identificadorConta: '' });
            await carregarDadosCarteira();
            setMensagem({ tipo: 'success', texto: 'Conta de corretora vinculada à carteira.' });
        } catch (error) {
            setMensagem({ tipo: 'error', texto: error.message });
        }
    };

    const registrarMovimentacao = async (event) => {
        event.preventDefault();
        setMensagem({ tipo: '', texto: '' });
        const exigeAtivo = tiposComAtivo.has(movimentacao.tipo);

        try {
            if (exigeAtivo && (!movimentacao.acaoId || Number(movimentacao.quantidade) <= 0 || Number(movimentacao.precoUnitario) <= 0)) {
                throw new Error('Informe ativo, quantidade e preço unitário para a operação.');
            }
            if (!exigeAtivo && Number(movimentacao.valor) <= 0) {
                throw new Error('Informe um valor maior que zero.');
            }
            const response = await apiFetch(`/carteiras/${carteiraId}/movimentacoes`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    ...movimentacao,
                    acaoId: movimentacao.acaoId || null,
                    contaCorretoraId: movimentacao.contaCorretoraId || null,
                    quantidade: exigeAtivo ? movimentacao.quantidade : null,
                    precoUnitario: exigeAtivo ? movimentacao.precoUnitario : null,
                    valor: exigeAtivo ? null : movimentacao.valor,
                    taxas: movimentacao.taxas || 0,
                    impostos: movimentacao.impostos || 0
                })
            });
            if (!response.ok) {
                throw new Error(await mensagemErro(response, 'Não foi possível registrar a movimentação.'));
            }
            setMovimentacao(novaMovimentacao());
            await carregarDadosCarteira();
            await carregarCarteiras(carteiraId);
            setMensagem({ tipo: 'success', texto: 'Movimentação registrada e carteira recalculada.' });
        } catch (error) {
            setMensagem({ tipo: 'error', texto: error.message });
        }
    };

    const cancelarMovimentacao = async (id) => {
        setMensagem({ tipo: '', texto: '' });
        try {
            const response = await apiFetch(`/carteiras/${carteiraId}/movimentacoes/${id}`, { method: 'DELETE' });
            if (!response.ok) {
                throw new Error(await mensagemErro(response, 'Não foi possível cancelar a movimentação.'));
            }
            await carregarDadosCarteira();
            await carregarCarteiras(carteiraId);
            setMensagem({ tipo: 'success', texto: 'Movimentação cancelada e carteira recalculada.' });
        } catch (error) {
            setMensagem({ tipo: 'error', texto: error.message });
        }
    };

    const resumo = useMemo(() => {
        const saldoAcao = Number(carteira?.saldoAcao || 0);
        const saldoEmConta = Number(carteira?.saldoEmConta || 0);
        const custoInvestido = posicoes.reduce((total, posicao) => total + Number(posicao.quantidadeTotal || 0) * Number(posicao.precoMedio || 0), 0);
        return {
            saldoAcao,
            saldoEmConta,
            saldoTotal: saldoAcao + saldoEmConta,
            resultado: saldoAcao - custoInvestido
        };
    }, [carteira, posicoes]);

    const tipoExigeAtivo = tiposComAtivo.has(movimentacao.tipo);
    const tipoAceitaAtivo = tipoExigeAtivo || tiposComAtivoOpcional.has(movimentacao.tipo);

    if (!isInvestor) {
        return (
            <div className="container">
                <Navbar title="Carteiras" />
                <p className="alert alert-warning">A gestão de carteiras e lançamentos pertence aos investidores. O administrador mantém o catálogo, as corretoras e as fontes de cotação.</p>
            </div>
        );
    }

    return (
        <div className="container">
            <Navbar title="Minhas Carteiras" />

            <section className="form-panel wallet-toolbar">
                <div className="field-group wallet-selector">
                    <label>Carteira selecionada</label>
                    <select value={carteiraId} onChange={(event) => setCarteiraId(event.target.value)} className="form-input" disabled={isLoading}>
                        {carteiras.map((item) => <option key={item.id} value={item.id}>{item.nome}{item.principal ? ' (Principal)' : ''}</option>)}
                    </select>
                </div>
                <form onSubmit={criarCarteira} className="app-form wallet-create-form">
                    <div className="field-group"><label>Nova carteira</label><input value={nomeCarteira} onChange={(event) => setNomeCarteira(event.target.value)} className="form-input" maxLength="80" placeholder="Ex.: Longo prazo" /></div>
                    <div className="form-actions"><button type="submit" className="btn btn-secondary" disabled={!nomeCarteira.trim()}>Criar carteira</button></div>
                </form>
            </section>

            {mensagem.texto && <p className={`alert ${mensagem.tipo === 'error' ? 'alert-error' : 'alert-success'}`}>{mensagem.texto}</p>}

            <main className="dashboard-cards carteira-summary">
                <article className="card"><h2>Investido</h2><p>Valor de mercado das posições.</p><div className="stat stat-money">{formatCurrency(resumo.saldoAcao)}</div></article>
                <article className="card"><h2>Disponível</h2><p>Saldo em conta da carteira.</p><div className="stat stat-money">{formatCurrency(resumo.saldoEmConta)}</div></article>
                <article className="card"><h2>Patrimônio</h2><p>Investimentos e saldo disponível.</p><div className="stat stat-money">{formatCurrency(resumo.saldoTotal)}</div></article>
                <article className="card"><h2>Resultado</h2><p>Valor de mercado menos custo investido.</p><div className={`stat stat-money ${resumo.resultado < 0 ? 'stat-negative' : ''}`}>{formatCurrency(resumo.resultado)}</div></article>
            </main>

            <section className="form-panel">
                <h3>Contas de Corretora</h3>
                <form onSubmit={criarConta} className="app-form">
                    <div className="field-group"><label>Corretora validada</label><select value={contaForm.corretoraId} onChange={(event) => setContaForm({ ...contaForm, corretoraId: event.target.value })} className="form-input"><option value="">Selecione...</option>{corretoras.map((item) => <option key={item.id} value={item.id}>{getNomeCorretora(item)}</option>)}</select></div>
                    <div className="field-group"><label>Apelido</label><input value={contaForm.apelido} onChange={(event) => setContaForm({ ...contaForm, apelido: event.target.value })} className="form-input" placeholder="Ex.: Conta XP" maxLength="80" /></div>
                    <div className="field-group"><label>Identificador da conta</label><input value={contaForm.identificadorConta} onChange={(event) => setContaForm({ ...contaForm, identificadorConta: event.target.value })} className="form-input" maxLength="80" /></div>
                    <div className="form-actions"><button type="submit" className="btn btn-secondary" disabled={!carteiraId || !contaForm.corretoraId || !contaForm.apelido.trim()}>Vincular conta</button></div>
                </form>
                {contas.length > 0 && <div className="account-list">{contas.map((conta) => <span key={conta.id} className="badge role-badge">{conta.apelido} · {getNomeCorretora(conta.corretora)}</span>)}</div>}
            </section>

            <section className="form-panel">
                <h3>Novo Lançamento</h3>
                <form onSubmit={registrarMovimentacao} className="app-form">
                    <div className="field-group"><label>Tipo</label><select value={movimentacao.tipo} onChange={(event) => setMovimentacao({ ...movimentacao, tipo: event.target.value, acaoId: '', quantidade: '', precoUnitario: '', valor: '' })} className="form-input">{tiposMovimentacao.map(([valor, rotulo]) => <option key={valor} value={valor}>{rotulo}</option>)}</select></div>
                    {tipoAceitaAtivo && <div className="field-group"><label>{tipoExigeAtivo ? 'Ativo' : 'Ativo relacionado'}</label><select value={movimentacao.acaoId} onChange={(event) => setMovimentacao({ ...movimentacao, acaoId: event.target.value })} className="form-input"><option value="">{tipoExigeAtivo ? 'Selecione...' : 'Não vincular'}</option>{catalogo.map((acao) => <option key={acao.id} value={acao.id}>{acao.ticker} - {acao.nomeEmpresa || 'Empresa'}</option>)}</select></div>}
                    <div className="field-group"><label>Conta de corretora</label><select value={movimentacao.contaCorretoraId} onChange={(event) => setMovimentacao({ ...movimentacao, contaCorretoraId: event.target.value })} className="form-input"><option value="">Não informar</option>{contas.map((conta) => <option key={conta.id} value={conta.id}>{conta.apelido}</option>)}</select></div>
                    <div className="field-group"><label>Data</label><input type="date" value={movimentacao.dataOperacao} onChange={(event) => setMovimentacao({ ...movimentacao, dataOperacao: event.target.value })} className="form-input" max={today()} /></div>
                    {tipoExigeAtivo ? <><div className="field-group"><label>Quantidade</label><input type="number" min="0.000001" step="0.000001" value={movimentacao.quantidade} onChange={(event) => setMovimentacao({ ...movimentacao, quantidade: event.target.value })} className="form-input" /></div><div className="field-group"><label>Preço unitário</label><input type="number" min="0.000001" step="0.000001" value={movimentacao.precoUnitario} onChange={(event) => setMovimentacao({ ...movimentacao, precoUnitario: event.target.value })} className="form-input" /></div></> : <div className="field-group"><label>Valor</label><input type="number" min="0.01" step="0.01" value={movimentacao.valor} onChange={(event) => setMovimentacao({ ...movimentacao, valor: event.target.value })} className="form-input" /></div>}
                    <div className="field-group"><label>Taxas</label><input type="number" min="0" step="0.01" value={movimentacao.taxas} onChange={(event) => setMovimentacao({ ...movimentacao, taxas: event.target.value })} className="form-input" /></div>
                    <div className="field-group"><label>Impostos</label><input type="number" min="0" step="0.01" value={movimentacao.impostos} onChange={(event) => setMovimentacao({ ...movimentacao, impostos: event.target.value })} className="form-input" /></div>
                    <div className="field-group"><label>Observação</label><input value={movimentacao.observacao} onChange={(event) => setMovimentacao({ ...movimentacao, observacao: event.target.value })} className="form-input" maxLength="500" /></div>
                    <div className="form-actions"><button type="submit" className="btn btn-primary" disabled={!carteiraId || isLoading}>Registrar lançamento</button></div>
                </form>
            </section>

            <section className="table-container">
                <h3>Posições</h3>
                {posicoes.length > 0 ? <table><thead><tr><th>Ativo</th><th>Quantidade</th><th>Preço médio</th><th>Cotação</th><th>Mercado</th><th>Posição</th></tr></thead><tbody>{posicoes.map((acao) => <tr key={acao.posicaoId}><td><span className="ticker-badge">{acao.ticker}</span> {acao.nomeEmpresa}</td><td>{acao.quantidadeTotal}</td><td>{formatCurrency(acao.precoMedio, acao.moeda)}</td><td>{formatCurrency(acao.cotacaoAtual, acao.moeda)}</td><td>{acao.mercado}</td><td className="price">{formatCurrency(acao.posicao, acao.moeda)}</td></tr>)}</tbody></table> : <div className="empty-state"><p>Nenhuma posição nesta carteira.</p></div>}
            </section>

            <section className="table-container movement-table">
                <h3>Histórico de Movimentações</h3>
                {movimentacoes.length > 0 ? <table><thead><tr><th>Data</th><th>Tipo</th><th>Ativo</th><th>Conta</th><th>Quantidade</th><th>Preço</th><th>Valor</th><th>Custos</th><th></th></tr></thead><tbody>{[...movimentacoes].reverse().map((item) => <tr key={item.id} className={item.cancelada ? 'cancelled-row' : ''}><td>{new Date(`${item.dataOperacao}T12:00:00`).toLocaleDateString('pt-BR')}</td><td>{item.tipo.replace('_', ' ')}</td><td>{item.ticker || '---'}</td><td>{item.contaCorretora || '---'}</td><td>{item.quantidade || '---'}</td><td>{item.precoUnitario ? formatCurrency(item.precoUnitario) : '---'}</td><td>{formatCurrency(item.valor)}</td><td>{formatCurrency(Number(item.taxas || 0) + Number(item.impostos || 0))}</td><td>{!item.cancelada && !item.tipo.startsWith('AJUSTE') && <button type="button" className="btn btn-secondary btn-small" onClick={() => cancelarMovimentacao(item.id)}>Cancelar</button>}</td></tr>)}</tbody></table> : <div className="empty-state"><p>Nenhum lançamento registrado.</p></div>}
            </section>
        </div>
    );
};

export default Carteiras;
