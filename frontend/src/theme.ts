import { createTheme } from "@mui/material/styles";
import type {} from "@mui/x-data-grid/themeAugmentation";

// Corona React Free Admin Template - Dark Theme
// Based on: https://github.com/nicedash/corona-react-free-admin-template

export const theme = createTheme({
  palette: {
    mode: "dark",
    primary: {
      main: "#0090e7",
      light: "#38a9ed",
      dark: "#0070b5",
      contrastText: "#ffffff",
    },
    secondary: {
      main: "#e4eaec",
      light: "#f0f4f5",
      dark: "#b8c2c5",
    },
    error: {
      main: "#fc424a",
      light: "#fd6b71",
      dark: "#d93038",
    },
    warning: {
      main: "#ffab00",
      light: "#ffbe33",
      dark: "#cc8900",
    },
    success: {
      main: "#00d25b",
      light: "#33db7c",
      dark: "#00a849",
    },
    info: {
      main: "#8f5fe8",
      light: "#a57fed",
      dark: "#724cba",
    },
    background: {
      default: "#000000",
      paper: "#191c24",
    },
    text: {
      primary: "#ffffff",
      secondary: "#6c7293",
    },
    divider: "#2c2e33",
    action: {
      hover: "rgba(255, 255, 255, 0.05)",
      selected: "rgba(255, 255, 255, 0.08)",
    },
  },
  shape: {
    borderRadius: 4,
  },
  typography: {
    fontFamily: '"Rubik", "Roboto", "Helvetica", "Arial", sans-serif',
    fontSize: 14,
    fontWeightLight: 300,
    fontWeightRegular: 400,
    fontWeightMedium: 500,
    fontWeightBold: 700,
    h1: { fontWeight: 500, color: "#ffffff" },
    h2: { fontWeight: 500, color: "#ffffff" },
    h3: { fontWeight: 500, color: "#ffffff" },
    h4: { fontWeight: 500, color: "#ffffff" },
    h5: { fontWeight: 500, color: "#ffffff" },
    h6: { fontWeight: 500, color: "#ffffff" },
    subtitle1: { color: "#6c7293" },
    subtitle2: { color: "#6c7293" },
    body1: { fontSize: "0.875rem" },
    body2: { fontSize: "0.875rem", color: "#6c7293" },
    button: { fontWeight: 500, textTransform: "none" },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          backgroundColor: "#000000",
          color: "#ffffff",
        },
      },
    },
    MuiAppBar: {
      defaultProps: {
        elevation: 0,
      },
      styleOverrides: {
        root: {
          backgroundColor: "#191c24",
          color: "#ffffff",
          boxShadow: "20px 19px 34px -15px rgba(0,0,0,0.5)",
          borderBottom: "none",
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundColor: "#191c24",
          color: "#6c7293",
          border: "none",
        },
      },
    },
    MuiCard: {
      defaultProps: {
        elevation: 0,
      },
      styleOverrides: {
        root: {
          backgroundColor: "#191c24",
          borderRadius: 4,
          border: "none",
        },
      },
    },
    MuiCardHeader: {
      styleOverrides: {
        title: {
          color: "#ffffff",
          fontSize: "1rem",
          fontWeight: 500,
        },
      },
    },
    MuiPaper: {
      defaultProps: {
        elevation: 0,
      },
      styleOverrides: {
        root: {
          backgroundColor: "#191c24",
          backgroundImage: "none",
        },
      },
    },
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
      styleOverrides: {
        root: {
          textTransform: "none",
          fontWeight: 500,
          borderRadius: 3,
          padding: "6px 12px",
          fontSize: "0.9375rem",
          lineHeight: 1,
        },
        containedPrimary: {
          "&:hover": {
            backgroundColor: "#0070b5",
          },
        },
      },
    },
    MuiIconButton: {
      styleOverrides: {
        root: {
          color: "#ffffff",
        },
      },
    },
    MuiTableContainer: {
      styleOverrides: {
        root: {
          backgroundColor: "#191c24",
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: "#191c24",
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          borderColor: "#2c2e33",
          color: "#ffffff",
          padding: "0.9375rem",
        },
        head: {
          fontWeight: 700,
          color: "#ffffff",
          fontSize: "0.875rem",
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          "&:hover": {
            backgroundColor: "rgba(255, 255, 255, 0.03)",
          },
        },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          backgroundColor: "#2A3038",
          borderRadius: 2,
          "&:hover .MuiOutlinedInput-notchedOutline": {
            borderColor: "#0090e7",
          },
        },
        notchedOutline: {
          borderColor: "#2c2e33",
        },
        input: {
          color: "#ffffff",
          fontSize: "0.875rem",
          padding: "0.56rem 0.75rem",
        },
      },
    },
    MuiInputLabel: {
      styleOverrides: {
        root: {
          color: "#6c7293",
        },
      },
    },
    MuiTextField: {
      defaultProps: {
        size: "small",
      },
    },
    MuiSelect: {
      styleOverrides: {
        icon: {
          color: "#6c7293",
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          fontWeight: 500,
          borderRadius: 3,
        },
      },
    },
    MuiDivider: {
      styleOverrides: {
        root: {
          borderColor: "#2c2e33",
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          color: "#6c7293",
          "&:hover": {
            backgroundColor: "rgba(255, 255, 255, 0.05)",
            color: "#ffffff",
          },
          "&.Mui-selected": {
            backgroundColor: "rgba(0, 144, 231, 0.1)",
            color: "#ffffff",
            "&:hover": {
              backgroundColor: "rgba(0, 144, 231, 0.15)",
            },
          },
        },
      },
    },
    MuiListItemIcon: {
      styleOverrides: {
        root: {
          color: "#bba8bff5",
          minWidth: 40,
        },
      },
    },
    MuiListItemText: {
      styleOverrides: {
        primary: {
          fontSize: "0.9375rem",
        },
      },
    },
    MuiTooltip: {
      styleOverrides: {
        tooltip: {
          backgroundColor: "#0d0d0d",
          borderRadius: 6,
          fontSize: "0.75rem",
          padding: "0.4rem 0.75rem",
        },
      },
    },
    MuiMenu: {
      styleOverrides: {
        paper: {
          backgroundColor: "#191c24",
          border: "1px solid #2c2e33",
        },
      },
    },
    MuiMenuItem: {
      styleOverrides: {
        root: {
          color: "#ffffff",
          "&:hover": {
            backgroundColor: "rgba(255, 255, 255, 0.05)",
          },
        },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          backgroundColor: "#000000",
          border: "1px solid #2c2e33",
        },
      },
    },
    MuiDialogTitle: {
      styleOverrides: {
        root: {
          borderBottom: "1px solid #2c2e33",
        },
      },
    },
    MuiDialogActions: {
      styleOverrides: {
        root: {
          borderTop: "1px solid #2c2e33",
        },
      },
    },
    MuiLinearProgress: {
      styleOverrides: {
        root: {
          backgroundColor: "#2f323a",
          borderRadius: 4,
        },
      },
    },
    MuiDataGrid: {
      styleOverrides: {
        root: {
          backgroundColor: "#191c24",
          border: "none",
          borderRadius: 4,
          "& .MuiDataGrid-cell": {
            borderColor: "#2c2e33",
            color: "#ffffff",
          },
          "& .MuiDataGrid-columnHeaders": {
            backgroundColor: "#191c24",
            borderColor: "#2c2e33",
          },
          "& .MuiDataGrid-footerContainer": {
            borderColor: "#2c2e33",
          },
          "& .MuiDataGrid-row:hover": {
            backgroundColor: "rgba(255, 255, 255, 0.03)",
          },
        },
      },
    },
    MuiAlert: {
      styleOverrides: {
        root: {
          borderRadius: 4,
        },
      },
    },
    MuiTabs: {
      styleOverrides: {
        indicator: {
          backgroundColor: "#0090e7",
        },
      },
    },
    MuiTab: {
      styleOverrides: {
        root: {
          textTransform: "none",
          color: "#6c7293",
          "&.Mui-selected": {
            color: "#ffffff",
          },
        },
      },
    },
    MuiBreadcrumbs: {
      styleOverrides: {
        separator: {
          color: "#6c7293",
        },
      },
    },
  },
});
