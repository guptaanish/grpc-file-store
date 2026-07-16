// The MUI dashboard template enables CSS theme variables (`cssVariables: true`
// in shared-theme/AppTheme.tsx) and reads `theme.vars` throughout its component
// and customization files. Enabling the `CssThemeVariables` module augmentation
// makes `theme.vars` available on the `Theme` type project-wide.
//
// Kept as a standalone declaration file so it survives re-syncing the vendored
// template folder from upstream.
import "@mui/material/styles";

declare module "@mui/material/styles" {
  interface CssThemeVariables {
    enabled: true;
  }
}
