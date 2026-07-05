import {
  Box,
  Button,
  Chip,
  Divider,
  Drawer,
  IconButton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import DownloadIcon from "@mui/icons-material/Download";
import { useFileMetadata, useFileVersions, useDownloadFile } from "../hooks/useFileStore";
import type { FileMetadata } from "../generated/filestore/v1/file_store_pb";

interface FileDetailDrawerProps {
  open: boolean;
  onClose: () => void;
  file: FileMetadata | null;
}

function formatDate(timestamp: { seconds: bigint } | undefined): string {
  if (!timestamp) return "—";
  return new Date(Number(timestamp.seconds) * 1000).toLocaleString();
}

function formatBytes(bytes: bigint | number): string {
  const b = Number(bytes);
  if (b === 0) return "0 B";
  const units = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(b) / Math.log(1024));
  return `${(b / Math.pow(1024, i)).toFixed(1)} ${units[i]}`;
}

export default function FileDetailDrawer({ open, onClose, file }: FileDetailDrawerProps) {
  const fileId = file?.fileId ?? null;
  const { data: metadata } = useFileMetadata(open ? fileId : null);
  const { data: versions } = useFileVersions(open ? fileId : null);
  const downloadMutation = useDownloadFile();

  const displayMetadata = metadata ?? file;

  return (
    <Drawer anchor="right" open={open} onClose={onClose} PaperProps={{ sx: { width: 450 } }}>
      <Box sx={{ p: 3 }}>
        {/* Header */}
        <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
          <Typography variant="h6" noWrap sx={{ maxWidth: 350 }}>
            {displayMetadata?.filename ?? "File Details"}
          </Typography>
          <IconButton onClick={onClose}>
            <CloseIcon />
          </IconButton>
        </Stack>

        {displayMetadata && (
          <>
            {/* Metadata */}
            <Stack spacing={1.5} sx={{ mb: 3 }}>
              <MetadataRow label="File ID" value={displayMetadata.fileId} mono />
              <MetadataRow label="Filename" value={displayMetadata.filename} />
              <MetadataRow label="Content Type" value={displayMetadata.contentType} />
              <MetadataRow label="Size" value={formatBytes(displayMetadata.size)} />
              <MetadataRow label="Version" value={String(displayMetadata.currentVersion)} />
              <MetadataRow label="Checksum" value={displayMetadata.checksum} mono />
              <MetadataRow label="Created" value={formatDate(displayMetadata.createdAt)} />
              <MetadataRow label="Updated" value={formatDate(displayMetadata.updatedAt)} />
            </Stack>

            {/* Download Button */}
            <Button
              variant="contained"
              startIcon={<DownloadIcon />}
              fullWidth
              sx={{ mb: 3 }}
              onClick={() => downloadMutation.mutate({ fileId: displayMetadata.fileId })}
              disabled={downloadMutation.isPending}
            >
              Download Latest
            </Button>

            <Divider sx={{ mb: 2 }} />

            {/* Version History */}
            <Typography variant="subtitle1" gutterBottom>
              Version History
            </Typography>

            {versions && versions.length > 0 ? (
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Ver</TableCell>
                    <TableCell>Size</TableCell>
                    <TableCell>Created</TableCell>
                    <TableCell></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {versions.map((v) => (
                    <TableRow key={v.version}>
                      <TableCell>
                        <Chip label={`v${v.version}`} size="small" />
                      </TableCell>
                      <TableCell>{formatBytes(v.size)}</TableCell>
                      <TableCell>{formatDate(v.createdAt)}</TableCell>
                      <TableCell>
                        <IconButton
                          size="small"
                          onClick={() =>
                            downloadMutation.mutate({
                              fileId: displayMetadata.fileId,
                              version: v.version,
                            })
                          }
                        >
                          <DownloadIcon fontSize="small" />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            ) : (
              <Typography variant="body2" color="text.secondary">
                No version history available.
              </Typography>
            )}
          </>
        )}
      </Box>
    </Drawer>
  );
}

function MetadataRow({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <Box>
      <Typography variant="caption" color="text.secondary">
        {label}
      </Typography>
      <Typography
        variant="body2"
        sx={{
          fontFamily: mono ? "monospace" : undefined,
          fontSize: mono ? "0.75rem" : undefined,
          wordBreak: "break-all",
        }}
      >
        {value}
      </Typography>
    </Box>
  );
}
