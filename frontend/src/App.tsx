import { Routes, Route } from "react-router-dom";
import AppShell from "./components/AppShell";
import FileBrowserPage from "./pages/FileBrowserPage";
import UploadPage from "./pages/UploadPage";

export default function App() {
  return (
    <AppShell>
      <Routes>
        <Route path="/" element={<FileBrowserPage />} />
        <Route path="/upload" element={<UploadPage />} />
      </Routes>
    </AppShell>
  );
}
