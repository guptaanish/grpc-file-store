import { type ReactNode } from "react";
import { Box } from "@mui/material";
import Sidebar from "./Sidebar";
import Navbar from "./Navbar";

interface LayoutProps {
  children: ReactNode;
}

export default function Layout({ children }: LayoutProps) {
  return (
    <Box sx={{ display: "flex", minHeight: "100vh", backgroundColor: "#000000" }}>
      {/* Sidebar - fixed left panel */}
      <Sidebar />

      {/* Page body wrapper - right side */}
      <Box
        sx={{
          flexGrow: 1,
          display: "flex",
          flexDirection: "column",
          minWidth: 0, // prevents flex item overflow
        }}
      >
        {/* Top navbar */}
        <Navbar />

        {/* Main content panel */}
        <Box
          component="main"
          sx={{
            flexGrow: 1,
            backgroundColor: "#000000",
            p: { xs: 2, sm: "1.875rem 1.75rem" }, // Corona's content-padding
            mt: "70px", // navbar height
            minHeight: "calc(100vh - 70px)",
          }}
        >
          {children}
        </Box>
      </Box>
    </Box>
  );
}
