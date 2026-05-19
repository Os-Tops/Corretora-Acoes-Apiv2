import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

// Aqui o React procura uma <div> com o id="root" no seu index.html
// e "injeta" toda a aplicação lá dentro.
ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);