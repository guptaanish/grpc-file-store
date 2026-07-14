import { useNavigate, useLocation } from "react-router-dom";
import {
  Box,
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Typography,
  Avatar,
} from "@mui/material";
import FolderIcon from "@mui/icons-material/Folder";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import StorageIcon from "@mui/icons-material/Storage";

const SIDEBAR_WIDTH = 244;

export default function Sidebar() {
  const navigate = useNavigate();
  const location = useLocation();

  const navItems = [
    { label: "File Browser", icon: <FolderIcon />, path: "/" },
    { label: "Upload", icon: <CloudUploadIcon />, path: "/upload" },
  ];

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: SIDEBAR_WIDTH,
        flexShrink: 0,
        "& .MuiDrawer-paper": {
          width: SIDEBAR_WIDTH,
          boxSizing: "border-box",
          backgroundColor: "#191c24",
          borderRight: "none",
          display: "flex",
          flexDirection: "column",
        },
      }}
    >
      {/* Brand */}
      <Box
        sx={{
          display: "flex",
          alignItems: "center",
          gap: 1.5,
          px: 2.5,
          py: 2.5,
          borderBottom: "1px solid #2c2e33",
        }}
      >
        <StorageIcon sx={{ color: "#0090e7", fontSize: 28 }} />
        <Typography
          variant="h6"
          sx={{
            color: "#ffffff",
            fontWeight: 700,
            fontSize: "1.1rem",
            letterSpacing: "0.05em",
            cursor: "pointer",
          }}
          onClick={() => navigate("/")}
        >
          FILE STORE
        </Typography>
      </Box>

      {/* User profile */}
      <Box
        sx={{
          display: "flex",
          alignItems: "center",
          gap: 1.5,
          px: 2.5,
          py: 2,
          borderBottom: "1px solid #2c2e33",
        }}
      >
        <Avatar
          sx={{
            width: 36,
            height: 36,
            bgcolor: "#0090e7",
            fontSize: "0.875rem",
          }}
        >
          A
        </Avatar>
        <Box>
          <Typography
            sx={{ color: "#ffffff", fontSize: "0.875rem", fontWeight: 500 }}
          >
            Admin
          </Typography>
          <Typography sx={{ color: "#6c7293", fontSize: "0.75rem" }}>
            Developer
          </Typography>
        </Box>
      </Box>

      {/* Navigation label */}
      <Typography
        sx={{
          color: "#6c7293",
          fontSize: "0.75rem",
          fontWeight: 500,
          textTransform: "uppercase",
          px: 2.5,
          pt: 2,
          pb: 0.5,
          letterSpacing: "0.05em",
        }}
      >
        Navigation
      </Typography>

      {/* Nav items */}
      <List sx={{ px: 1.5, flex: 1 }}>
        {navItems.map((item) => {
          const isActive = location.pathname === item.path;
          return (
            <ListItemButton
              key={item.path}
              onClick={() => navigate(item.path)}
              selected={isActive}
              sx={{
                borderRadius: 1,
                mb: 0.5,
                py: 1,
                px: 1.5,
                color: isActive ? "#ffffff" : "#6c7293",
                backgroundColor: isActive
                  ? "rgba(0, 144, 231, 0.1)"
                  : "transparent",
                "&:hover": {
                  backgroundColor: isActive
                    ? "rgba(0, 144, 231, 0.15)"
                    : "rgba(255, 255, 255, 0.05)",
                  color: "#ffffff",
                },
                "&.Mui-selected": {
                  backgroundColor: "rgba(0, 144, 231, 0.1)",
                  color: "#ffffff",
                },
                "&.Mui-selected:hover": {
                  backgroundColor: "rgba(0, 144, 231, 0.15)",
                },
              }}
            >
              <ListItemIcon
                sx={{
                  color: isActive ? "#0090e7" : "#bba8bff5",
                  minWidth: 36,
                }}
              >
                {item.icon}
              </ListItemIcon>
              <ListItemText
                primary={item.label}
                primaryTypographyProps={{
                  fontSize: "0.9375rem",
                  fontWeight: isActive ? 500 : 400,
                }}
              />
            </ListItemButton>
          );
        })}
      </List>
    </Drawer>
  );
}

export { SIDEBAR_WIDTH };
