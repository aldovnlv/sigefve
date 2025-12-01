const { createProxyMiddleware } = require("http-proxy-middleware");

module.exports = function (app) {

  // Proxy para /go/*
  app.use(
    "/go",
    createProxyMiddleware({
      target: "https://apisigefve.xipatlani.tk",
      changeOrigin: true,
      secure: false,
    })
  );

  // Proxy para /java/*
  app.use(
    "/java",
    createProxyMiddleware({
      target: "https://apisigefve.xipatlani.tk",
      changeOrigin: true,
      secure: false,
    })
  );

  // Proxy para /python/*
  app.use(
    "/python",
    createProxyMiddleware({
      target: "https://apisigefve.xipatlani.tk",
      changeOrigin: true,
      secure: false,
    })
  );

};
