import { Suspense, lazy } from "react";
import { Routes, Route } from "react-router-dom";
import { Box, CircularProgress } from "@mui/material";
import Layout from "./components/Layout";

// Route-based code splitting: each page is a separate chunk so the initial
// bundle stays small. The Dashboard template (charts, tree view, date pickers,
// react-spring) is especially heavy and only loaded when its route is visited.
const FileBrowserPage = lazy(() => import("./pages/FileBrowserPage"));
const UploadPage = lazy(() => import("./pages/UploadPage"));
const Dashboard = lazy(() => import("./dashboard-template/dashboard/Dashboard"));

function RouteFallback() {
  return (
    <Box
      sx={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        minHeight: "60vh",
      }}
    >
      <CircularProgress />
    </Box>
  );
}

export default function App() {
  return (
    <Suspense fallback={<RouteFallback />}>
      <Routes>
        {/* Standalone MUI dashboard template — renders its own theme + shell */}
        <Route path="/dashboard" element={<Dashboard />} />
        {/* App pages share the existing Corona layout */}
        <Route
          path="/"
          element={
            <Layout>
              <FileBrowserPage />
            </Layout>
          }
        />
        <Route
          path="/upload"
          element={
            <Layout>
              <UploadPage />
            </Layout>
          }
        />
      </Routes>
    </Suspense>
  );
}
