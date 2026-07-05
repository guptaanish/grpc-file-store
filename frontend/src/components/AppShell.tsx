import { type ReactNode } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import {
  AppBar,
  Box,
  Button,
  Container,
  Toolbar,
  Typography,
} from "@mui/material";
import FolderIcon from "@mui/icons-material/Folder";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";

interface AppShellProps {
  children: ReactNode;
}

export default function AppShell({ children }: AppShellProps) {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <Box sx={{ display: "flex", flexDirection: "column", minHeight: "100vh" }}>
      <AppBar position="static">
        <Toolbar>
          <FolderIcon sx={{ mr: 1 }} />
          <Typography
            variant="h6"
            component="div"
            sx={{ cursor: "pointer", mr: 4 }}
            onClick={() => navigate("/")}
          >
            File Store
          </Typography>
          <Button
            color="inherit"
            startIcon={<FolderIcon />}
            onClick={() => navigate("/")}
            variant={location.pathname === "/" ? "outlined" : "text"}
            sx={{
              mr: 1,
              borderColor: "rgba(255,255,255,0.5)",
            }}
          >
            Files
          </Button>
          <Button
            color="inherit"
            startIcon={<CloudUploadIcon />}
            onClick={() => navigate("/upload")}
            variant={location.pathname === "/upload" ? "outlined" : "text"}
            sx={{ borderColor: "rgba(255,255,255,0.5)" }}
          >
            Upload
          </Button>
        </Toolbar>
      </AppBar>
      <Container maxWidth="xl" sx={{ mt: 3, mb: 3, flex: 1 }}>
        {children}
      </Container>
    </Box>
  );
}
