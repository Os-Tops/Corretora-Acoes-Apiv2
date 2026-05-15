import React from 'react';
import { NavLink } from 'react-router-dom';

const Navbar = ({ title }) => {
    return (
        <header className="header">
            <h1>{title}</h1>
            <nav className="nav-links">
                <NavLink to="/" end className={({ isActive }) => isActive ? "active" : ""}>
                    Dashboard
                </NavLink>
                <NavLink to="/corretoras" className={({ isActive }) => isActive ? "active" : ""}>
                    Corretoras
                </NavLink>
                <NavLink to="/acoes" className={({ isActive }) => isActive ? "active" : ""}>
                    Ações
                </NavLink>
            </nav>
        </header>
    );
};

export default Navbar;