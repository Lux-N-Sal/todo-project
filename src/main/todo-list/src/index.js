import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import { CookiesProvider } from "react-cookie";
import { ListsProvider } from './context/listsContext';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <ListsProvider>
  <React.StrictMode>
    <CookiesProvider>
      <App />
    </CookiesProvider>
  </React.StrictMode>
  </ListsProvider>
);