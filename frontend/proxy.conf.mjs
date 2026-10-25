// Proxy do servidor de desenvolvimento: as chamadas a /api são repassadas ao backend Spring Boot.
// Porta padrão 8080; para usar outra, defina API_PORT antes do `npm start` (ex.: API_PORT=8081).
const porta = process.env.API_PORT || '8080';

export default {
  '/api': {
    target: `http://localhost:${porta}`,
    secure: false,
    changeOrigin: true,
  },
};
