import React, { useState, useEffect } from 'react';
import Navbar from '../components/Navbar';
import { Link } from 'react-router-dom';

const Dashboard = () => {
    // Criamos o estado para guardar os números que vêm do back
    const [stats, setStats] = useState({ corretorasCount: 0, acoesCount: 0 });

    useEffect(() => {
        // Esta função roda assim que a página abre
        fetch('http://localhost:8080/api/stats')
            .then(response => response.json())
            .then(data => setStats(data))
            .catch(error => console.error("Erro ao buscar dados:", error));
    }, []);

    return (
        <div className="container">
            <Navbar title="Gestão Financeira" />

            <main className="dashboard-cards">
                {/* IMPORTANTE: Use className e não class */}
                <Link to="/corretoras" className="card">
                    <h2>Corretoras Cadastradas</h2>
                    <p>Gerencie as corretoras parceiras validadas junto à CVM.</p>
                    <div className="stat">{stats.corretorasCount}</div>
                </Link>

                <Link to="/acoes" className="card acoes">
                    <h2>Ações Monitoradas</h2>
                    <p>Acompanhe os ativos financeiros do mercado BR e US.</p>
                    <div className="stat">{stats.acoesCount}</div>
                </Link>
            </main>
        </div>
    );
};

export default Dashboard;