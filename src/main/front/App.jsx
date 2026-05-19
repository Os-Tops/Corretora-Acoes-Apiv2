import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

// Importamos as páginas que criamos anteriormente
import Dashboard from './pages/Dashboard';
import Corretoras from './pages/Corretoras';
import Acoes from './pages/Acoes';

// Importamos o CSS global
import './styles/index.css';

function App() {
    return (
        <Router>
            <Routes>
                {/* Rota Principal: Dashboard */}
                <Route path="/" element={<Dashboard />} />

                {/* Rota para a lista de Corretoras */}
                <Route path="/corretoras" element={<Corretoras />} />

                {/* Rota para a lista de Ações */}
                <Route path="/acoes" element={<Acoes />} />

                {/* Caso o utilizador tente aceder ao antigo /dashboard, redirecionamos para / */}
                <Route path="/dashboard" element={<Navigate to="/" replace />} />
            </Routes>
        </Router>
    );
}

export default App;