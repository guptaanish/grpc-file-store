import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  build: {
    rollupOptions: {
      output: {
        // Split large, rarely-changing vendor code into per-package chunks so no
        // single chunk trips the size-warning threshold and browser caching works
        // better across app deploys. Heavy MUI X packages are isolated because
        // they are each only needed by specific routes.
        manualChunks(id) {
          if (!id.includes("node_modules")) {
            return undefined;
          }
          if (id.includes("@mui/x-charts") || id.includes("/d3-")) {
            return "vendor-mui-x-charts";
          }
          if (id.includes("@mui/x-data-grid")) {
            return "vendor-mui-x-datagrid";
          }
          if (id.includes("@mui/x-date-pickers") || id.includes("@mui/x-tree-view")) {
            return "vendor-mui-x-pickers";
          }
          if (id.includes("@mui/") || id.includes("@emotion/")) {
            return "vendor-mui";
          }
          if (
            id.includes("/react/") ||
            id.includes("/react-dom/") ||
            id.includes("/react-router") ||
            id.includes("/scheduler/")
          ) {
            return "vendor-react";
          }
          return undefined;
        },
      },
    },
  },
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
