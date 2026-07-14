import { Routes, Route } from "react-router-dom";
import Layout from "./components/Layout";
import FileBrowserPage from "./pages/FileBrowserPage";
import UploadPage from "./pages/UploadPage";

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<FileBrowserPage />} />
        <Route path="/upload" element={<UploadPage />} />
      </Routes>
    </Layout>
  );
}
