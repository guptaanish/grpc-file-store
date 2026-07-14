import { useState, useCallback } from "react";
import {
  Box,
  TextField,
  InputAdornment,
  IconButton,
  Paper,
  Typography,
  Tooltip,
  Stack,
  Chip,
} from "@mui/material";
import { DataGrid, type GridColDef, type GridRowParams } from "@mui/x-data-grid";
import SearchIcon from "@mui/icons-material/Search";
import DownloadIcon from "@mui/icons-material/Download";
import DeleteIcon from "@mui/icons-material/Delete";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import DriveFileMoveIcon from "@mui/icons-material/DriveFileMove";
import NavigateNextIcon from "@mui/icons-material/NavigateNext";
import NavigateBeforeIcon from "@mui/icons-material/NavigateBefore";
import { useListFiles, useDownloadFile, useDeleteFile } from "../hooks/useFileStore";
import type { FileMetadata } from "../generated/filestore/v1/file_store_pb";
import FileDetailDrawer from "../components/FileDetailDrawer";
import CopyDialog from "../components/CopyDialog";
import MoveDialog from "../components/MoveDialog";
import DeleteDialog from "../components/DeleteDialog";

function formatBytes(bytes: bigint | number): string {
  const b = Number(bytes);
  if (b === 0) return "0 B";
  const units = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(b) / Math.log(1024));
  return `${(b / Math.pow(1024, i)).toFixed(1)} ${units[i]}`;
}

function formatDate(timestamp: { seconds: bigint } | undefined): string {
  if (!timestamp) return "—";
  return new Date(Number(timestamp.seconds) * 1000).toLocaleString();
}

export default function FileBrowserPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [debouncedQuery, setDebouncedQuery] = useState("");
  const [pageToken, setPageToken] = useState("");
  const [pageHistory, setPageHistory] = useState<string[]>([""]);
  const [selectedFile, setSelectedFile] = useState<FileMetadata | null>(null);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [copyFile, setCopyFile] = useState<FileMetadata | null>(null);
  const [moveFile, setMoveFile] = useState<FileMetadata | null>(null);
  const [deleteFile, setDeleteFile] = useState<FileMetadata | null>(null);

  const pageSize = 25;
  const { data, isLoading, error } = useListFiles(debouncedQuery, pageSize, pageToken);
  const downloadMutation = useDownloadFile();
  const deleteMutation = useDeleteFile();

  const handleSearch = useCallback(() => {
    setDebouncedQuery(searchQuery);
    setPageToken("");
    setPageHistory([""]);
  }, [searchQuery]);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (e.key === "Enter") handleSearch();
    },
    [handleSearch],
  );

  const handleNextPage = () => {
    if (data?.nextPageToken) {
      setPageHistory((prev) => [...prev, data.nextPageToken]);
      setPageToken(data.nextPageToken);
    }
  };

  const handlePrevPage = () => {
    if (pageHistory.length > 1) {
      const newHistory = [...pageHistory];
      newHistory.pop();
      const prevToken = newHistory[newHistory.length - 1] ?? "";
      setPageHistory(newHistory);
      setPageToken(prevToken);
    }
  };

  const handleRowClick = (params: GridRowParams) => {
    setSelectedFile(params.row as FileMetadata);
    setDrawerOpen(true);
  };

  const columns: GridColDef[] = [
    {
      field: "filename",
      headerName: "Filename",
      flex: 2,
      minWidth: 200,
    },
    {
      field: "contentType",
      headerName: "Type",
      flex: 1,
      minWidth: 120,
      renderCell: (params) => (
        <Chip
          label={params.value as string}
          size="small"
          variant="outlined"
          sx={{ maxWidth: 150 }}
        />
      ),
    },
    {
      field: "size",
      headerName: "Size",
      width: 100,
      valueFormatter: (value: bigint) => formatBytes(value),
    },
    {
      field: "currentVersion",
      headerName: "Version",
      width: 80,
      align: "center",
      headerAlign: "center",
    },
    {
      field: "updatedAt",
      headerName: "Updated",
      flex: 1,
      minWidth: 160,
      valueFormatter: (value: { seconds: bigint } | undefined) => formatDate(value),
    },
    {
      field: "actions",
      headerName: "Actions",
      width: 180,
      sortable: false,
      renderCell: (params) => {
        const file = params.row as FileMetadata;
        return (
          <Stack direction="row" spacing={0.5}>
            <Tooltip title="Download">
              <IconButton
                size="small"
                onClick={(e) => {
                  e.stopPropagation();
                  downloadMutation.mutate({ fileId: file.fileId });
                }}
              >
                <DownloadIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Tooltip title="Copy">
              <IconButton
                size="small"
                onClick={(e) => {
                  e.stopPropagation();
                  setCopyFile(file);
                }}
              >
                <ContentCopyIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Tooltip title="Move/Rename">
              <IconButton
                size="small"
                onClick={(e) => {
                  e.stopPropagation();
                  setMoveFile(file);
                }}
              >
                <DriveFileMoveIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            <Tooltip title="Delete">
              <IconButton
                size="small"
                color="error"
                onClick={(e) => {
                  e.stopPropagation();
                  setDeleteFile(file);
                }}
              >
                <DeleteIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          </Stack>
        );
      },
    },
  ];

  const rows =
    data?.files.map((file) => ({
      id: file.fileId,
      ...file,
    })) ?? [];

  return (
    <Box>
      {/* Page Header */}
      <Typography variant="h5" sx={{ color: "#ffffff", mb: 2, fontWeight: 500 }}>
        File Browser
      </Typography>

      {/* Search Bar */}
      <Box sx={{ mb: 2 }}>
        <TextField
          fullWidth
          placeholder="Search files by name..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onKeyDown={handleKeyDown}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon sx={{ color: "#6c7293" }} />
              </InputAdornment>
            ),
            endAdornment: (
              <InputAdornment position="end">
                <IconButton onClick={handleSearch} edge="end" sx={{ color: "#6c7293" }}>
                  <SearchIcon />
                </IconButton>
              </InputAdornment>
            ),
          }}
          size="small"
        />
      </Box>

      {/* Error Display */}
      {error && (
        <Paper sx={{ p: 2, mb: 2, bgcolor: "rgba(252, 66, 74, 0.1)", border: "1px solid #fc424a" }}>
          <Typography sx={{ color: "#fc424a" }}>Error loading files: {error.message}</Typography>
        </Paper>
      )}

      {/* Data Grid */}
      <Paper sx={{ height: 600, width: "100%" }}>
        <DataGrid
          rows={rows}
          columns={columns}
          loading={isLoading}
          onRowClick={handleRowClick}
          disableRowSelectionOnClick
          hideFooter
          sx={{
            border: "none",
            "& .MuiDataGrid-row:hover": {
              cursor: "pointer",
              backgroundColor: "rgba(255, 255, 255, 0.03)",
            },
            "& .MuiDataGrid-cell": {
              borderColor: "#2c2e33",
            },
            "& .MuiDataGrid-columnHeaders": {
              borderColor: "#2c2e33",
            },
          }}
        />
      </Paper>

      {/* Pagination Controls */}
      <Box sx={{ display: "flex", justifyContent: "flex-end", alignItems: "center", mt: 1, gap: 1 }}>
        <Typography variant="body2" sx={{ color: "#6c7293" }}>
          Page {pageHistory.length}
        </Typography>
        <IconButton
          onClick={handlePrevPage}
          disabled={pageHistory.length <= 1}
          size="small"
          sx={{ color: "#6c7293" }}
        >
          <NavigateBeforeIcon />
        </IconButton>
        <IconButton
          onClick={handleNextPage}
          disabled={!data?.nextPageToken}
          size="small"
          sx={{ color: "#6c7293" }}
        >
          <NavigateNextIcon />
        </IconButton>
      </Box>

      {/* File Detail Drawer */}
      <FileDetailDrawer
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        file={selectedFile}
      />

      {/* Action Dialogs */}
      <CopyDialog file={copyFile} onClose={() => setCopyFile(null)} />
      <MoveDialog file={moveFile} onClose={() => setMoveFile(null)} />
      <DeleteDialog
        file={deleteFile}
        onClose={() => setDeleteFile(null)}
        onConfirm={(fileId) => {
          deleteMutation.mutate(fileId);
          setDeleteFile(null);
        }}
      />
    </Box>
  );
}
