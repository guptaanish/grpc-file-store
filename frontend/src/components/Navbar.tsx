import {
  AppBar,
  Avatar,
  Box,
  IconButton,
  InputAdornment,
  TextField,
  Toolbar,
  Typography,
} from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import NotificationsNoneIcon from "@mui/icons-material/NotificationsNone";
import MailOutlineIcon from "@mui/icons-material/MailOutline";
import FullscreenIcon from "@mui/icons-material/Fullscreen";
import { SIDEBAR_WIDTH } from "./Sidebar";

export default function Navbar() {
  return (
    <AppBar
      position="fixed"
      elevation={0}
      sx={{
        width: `calc(100% - ${SIDEBAR_WIDTH}px)`,
        ml: `${SIDEBAR_WIDTH}px`,
        backgroundColor: "#191c24",
        borderBottom: "none",
        boxShadow: "20px 19px 34px -15px rgba(0,0,0,0.5)",
      }}
    >
      <Toolbar
        sx={{
          minHeight: 70,
          px: { xs: 2, sm: 3 },
          justifyContent: "space-between",
        }}
      >
        {/* Search bar */}
        <TextField
          placeholder="Search files..."
          size="small"
          sx={{
            width: { xs: 200, sm: 300, md: 400 },
            "& .MuiOutlinedInput-root": {
              backgroundColor: "#2A3038",
              borderRadius: 1,
              "& fieldset": {
                borderColor: "#2c2e33",
              },
              "&:hover fieldset": {
                borderColor: "#0090e7",
              },
              "&.Mui-focused fieldset": {
                borderColor: "#0090e7",
              },
            },
            "& .MuiInputBase-input": {
              color: "#ffffff",
              fontSize: "0.875rem",
              "&::placeholder": {
                color: "#6c7293",
                opacity: 1,
              },
            },
          }}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon sx={{ color: "#6c7293", fontSize: 20 }} />
              </InputAdornment>
            ),
          }}
        />

        {/* Right side actions */}
        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
          <IconButton sx={{ color: "#6c7293" }}>
            <FullscreenIcon />
          </IconButton>
          <IconButton sx={{ color: "#6c7293" }}>
            <MailOutlineIcon />
          </IconButton>
          <IconButton sx={{ color: "#6c7293" }}>
            <NotificationsNoneIcon />
          </IconButton>

          {/* User profile */}
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              gap: 1,
              ml: 1,
              pl: 2,
              borderLeft: "1px solid #2c2e33",
            }}
          >
            <Avatar
              sx={{
                width: 32,
                height: 32,
                bgcolor: "#0090e7",
                fontSize: "0.75rem",
              }}
            >
              A
            </Avatar>
            <Typography
              sx={{
                color: "#ffffff",
                fontSize: "0.875rem",
                display: { xs: "none", sm: "block" },
              }}
            >
              Admin
            </Typography>
          </Box>
        </Box>
      </Toolbar>
    </AppBar>
  );
}
