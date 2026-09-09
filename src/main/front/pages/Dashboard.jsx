import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { apiFetch } from '../services/api';

const formatCurrency = (value) => Number(value || 0).toLocaleString('pt-BR', {
    style: 'currency',
    currency: 'BRL'
});

const Dashboard = () => {
    const [stats, setStats] = useState({
        corretorasCount: 0,
        acoesCount: 0,
        carteirasCount: 0,
        saldoAcao: 0,
        saldoEmConta: 0,
        saldoTotal: 0,
        resultadoNaoRealizado: 0
    });

    useEffect(() => {
        apiFetch('/api/dashboard/stats')
            .then((response) => response.json())
            .then((data) => setStats(data))
            .catch((error) => console.error('Erro ao buscar dados:', error));
    }, []);

    return (
        <div className="container">
            <Navbar title="Gestão Financeira" />

            <main className="dashboard-cards">
                <Link to="/carteiras" className="card">
                    <h2>Patrimônio Consolidado</h2>
                    <p>Investimentos e saldo disponível em todas as carteiras.</p>
                    <div className="stat stat-money">{formatCurrency(stats.saldoTotal)}</div>
                </Link>

                <Link to="/carteiras" className="card">
                    <h2>Resultado Não Realizado</h2>
                    <p>Valor de mercado menos custo das posições abertas.</p>
                    <div className={`stat stat-money ${Number(stats.resultadoNaoRealizado) < 0 ? 'stat-negative' : ''}`}>{formatCurrency(stats.resultadoNaoRealizado)}</div>
                </Link>

                <Link to="/corretoras" className="card">
                    <h2>Corretoras Cadastradas</h2>
                    <p>Total de corretoras cadastradas no sistema.</p>
                    <div className="stat">{stats.corretorasCount}</div>
                </Link>

                <Link to="/acoes" className="card acoes">
                    <h2>Posições Abertas</h2>
                    <p>Total de posições ativas nas suas carteiras.</p>
                    <div className="stat">{stats.acoesCount}</div>
                </Link>
            </main>
        </div>
    );
};

export default Dashboard;
