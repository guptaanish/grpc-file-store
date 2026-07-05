import { useState, useCallback } from "react";
import {
  Box,
  Paper,
  Typography,
  LinearProgress,
  Alert,
  Card,
  CardContent,
  Chip,
  Stack,
  Switch,
  FormControlLabel,
} from "@mui/material";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import { useDropzone } from "react-dropzone";
import {
  useUploadFile,
  useInitiateResumableUpload,
  useResumeUploadChunk,
  type UploadProgress,
  type UploadResult,
} from "../hooks/useFileStore";

const CHUNK_SIZE = 256 * 1024; // 256 KB chunks for resumable upload

export default function UploadPage() {
  const [progress, setProgress] = useState<UploadProgress | null>(null);
  const [result, setResult] = useState<UploadResult | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [resumable, setResumable] = useState(false);

  const uploadMutation = useUploadFile();
  const initiateMutation = useInitiateResumableUpload();
  const resumeChunkMutation = useResumeUploadChunk();

  const handleSimpleUpload = useCallback(
    async (file: File) => {
      setProgress(null);
      setResult(null);
      setError(null);

      try {
        const uploadResult = await uploadMutation.mutateAsync({
          file,
          onProgress: setProgress,
        });
        setResult(uploadResult);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Upload failed");
      }
    },
    [uploadMutation],
  );

  const handleResumableUpload = useCallback(
    async (file: File) => {
      setProgress(null);
      setResult(null);
      setError(null);

      try {
        // Step 1: Initiate session
        const session = await initiateMutation.mutateAsync({
          filename: file.name,
          contentType: file.type || "application/octet-stream",
          totalSize: file.size,
        });

        // Step 2: Send chunks
        let offset = 0;
        while (offset < file.size) {
          const end = Math.min(offset + CHUNK_SIZE, file.size);
          const chunk = file.slice(offset, end);

          setProgress({
            loaded: offset,
            total: file.size,
            percent: Math.round((offset / file.size) * 100),
          });

          const chunkResult = await resumeChunkMutation.mutateAsync({
            sessionId: session.sessionId,
            chunk,
            offset,
          });

          // If the response has a fileId, upload is complete
          if ("fileId" in chunkResult) {
            setResult(chunkResult as UploadResult);
            setProgress({ loaded: file.size, total: file.size, percent: 100 });
            return;
          }

          offset = end;
        }

        setProgress({ loaded: file.size, total: file.size, percent: 100 });
      } catch (err) {
        setError(err instanceof Error ? err.message : "Resumable upload failed");
      }
    },
    [initiateMutation, resumeChunkMutation],
  );

  const onDrop = useCallback(
    (acceptedFiles: File[]) => {
      const file = acceptedFiles[0];
      if (!file) return;

      if (resumable) {
        handleResumableUpload(file);
      } else {
        handleSimpleUpload(file);
      }
    },
    [resumable, handleSimpleUpload, handleResumableUpload],
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    multiple: false,
    maxSize: 100 * 1024 * 1024, // 100 MB
  });

  const isUploading = uploadMutation.isPending || initiateMutation.isPending || resumeChunkMutation.isPending;

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Upload File
      </Typography>

      <FormControlLabel
        control={
          <Switch
            checked={resumable}
            onChange={(e) => setResumable(e.target.checked)}
            disabled={isUploading}
          />
        }
        label="Use resumable upload (for large files)"
        sx={{ mb: 2 }}
      />

      {/* Drop Zone */}
      <Paper
        {...getRootProps()}
        sx={{
          p: 6,
          mb: 3,
          textAlign: "center",
          cursor: "pointer",
          border: "2px dashed",
          borderColor: isDragActive ? "primary.main" : "grey.400",
          bgcolor: isDragActive ? "action.hover" : "background.paper",
          transition: "all 0.2s ease",
          "&:hover": {
            borderColor: "primary.main",
            bgcolor: "action.hover",
          },
        }}
      >
        <input {...getInputProps()} />
        <CloudUploadIcon sx={{ fontSize: 64, color: "primary.main", mb: 2 }} />
        <Typography variant="h6" gutterBottom>
          {isDragActive ? "Drop the file here" : "Drag & drop a file here, or click to browse"}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Maximum file size: 100 MB
          {resumable && " • Using resumable upload (256 KB chunks)"}
        </Typography>
      </Paper>

      {/* Progress Bar */}
      {progress && !result && (
        <Paper sx={{ p: 2, mb: 2 }}>
          <Typography variant="body2" gutterBottom>
            Uploading... {progress.percent}%
          </Typography>
          <LinearProgress variant="determinate" value={progress.percent} sx={{ height: 8, borderRadius: 4 }} />
          <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5 }}>
            {formatBytes(progress.loaded)} / {formatBytes(progress.total)}
          </Typography>
        </Paper>
      )}

      {/* Error */}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* Success Result */}
      {result && (
        <Card sx={{ bgcolor: "success.light" }}>
          <CardContent>
            <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
              <CheckCircleIcon color="success" />
              <Typography variant="h6">Upload Complete</Typography>
            </Stack>
            <Stack spacing={1}>
              <Typography>
                <strong>File:</strong> {result.filename}
              </Typography>
              <Typography>
                <strong>Size:</strong> {formatBytes(result.size)}
              </Typography>
              <Typography>
                <strong>Version:</strong> {result.version}
              </Typography>
              <Stack direction="row" spacing={1} alignItems="center">
                <Typography>
                  <strong>Checksum:</strong>
                </Typography>
                <Chip label={result.checksum.slice(0, 16) + "..."} size="small" variant="outlined" />
              </Stack>
              <Typography variant="caption" color="text.secondary">
                File ID: {result.fileId}
              </Typography>
            </Stack>
          </CardContent>
        </Card>
      )}
    </Box>
  );
}

function formatBytes(bytes: number): string {
  if (bytes === 0) return "0 B";
  const units = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  return `${(bytes / Math.pow(1024, i)).toFixed(1)} ${units[i]}`;
}
