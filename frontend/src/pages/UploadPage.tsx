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
      <Typography variant="h5" sx={{ color: "#ffffff", mb: 1, fontWeight: 500 }}>
        Upload File
      </Typography>

      <FormControlLabel
        control={
          <Switch
            checked={resumable}
            onChange={(e) => setResumable(e.target.checked)}
            disabled={isUploading}
            color="primary"
          />
        }
        label="Use resumable upload (for large files)"
        sx={{ mb: 2, color: "#6c7293" }}
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
          borderColor: isDragActive ? "#0090e7" : "#2c2e33",
          bgcolor: isDragActive ? "rgba(0, 144, 231, 0.05)" : "#191c24",
          transition: "all 0.2s ease",
          "&:hover": {
            borderColor: "#0090e7",
            bgcolor: "rgba(0, 144, 231, 0.05)",
          },
        }}
      >
        <input {...getInputProps()} />
        <CloudUploadIcon sx={{ fontSize: 64, color: "#0090e7", mb: 2 }} />
        <Typography variant="h6" sx={{ color: "#ffffff" }} gutterBottom>
          {isDragActive ? "Drop the file here" : "Drag & drop a file here, or click to browse"}
        </Typography>
        <Typography variant="body2" sx={{ color: "#6c7293" }}>
          Maximum file size: 100 MB
          {resumable && " • Using resumable upload (256 KB chunks)"}
        </Typography>
      </Paper>

      {/* Progress Bar */}
      {progress && !result && (
        <Paper sx={{ p: 2, mb: 2, bgcolor: "#191c24" }}>
          <Typography variant="body2" sx={{ color: "#ffffff" }} gutterBottom>
            Uploading... {progress.percent}%
          </Typography>
          <LinearProgress
            variant="determinate"
            value={progress.percent}
            sx={{
              height: 8,
              borderRadius: 4,
              backgroundColor: "#2f323a",
              "& .MuiLinearProgress-bar": {
                backgroundColor: "#0090e7",
              },
            }}
          />
          <Typography variant="caption" sx={{ color: "#6c7293", mt: 0.5 }}>
            {formatBytes(progress.loaded)} / {formatBytes(progress.total)}
          </Typography>
        </Paper>
      )}

      {/* Error */}
      {error && (
        <Alert severity="error" sx={{ mb: 2, bgcolor: "rgba(252, 66, 74, 0.1)", color: "#fc424a" }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* Success Result */}
      {result && (
        <Card sx={{ bgcolor: "rgba(0, 210, 91, 0.1)", border: "1px solid #00d25b" }}>
          <CardContent>
            <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
              <CheckCircleIcon sx={{ color: "#00d25b" }} />
              <Typography variant="h6" sx={{ color: "#ffffff" }}>Upload Complete</Typography>
            </Stack>
            <Stack spacing={1}>
              <Typography sx={{ color: "#ffffff" }}>
                <strong>File:</strong> {result.filename}
              </Typography>
              <Typography sx={{ color: "#ffffff" }}>
                <strong>Size:</strong> {formatBytes(result.size)}
              </Typography>
              <Typography sx={{ color: "#ffffff" }}>
                <strong>Version:</strong> {result.version}
              </Typography>
              <Stack direction="row" spacing={1} alignItems="center">
                <Typography sx={{ color: "#ffffff" }}>
                  <strong>Checksum:</strong>
                </Typography>
                <Chip
                  label={result.checksum.slice(0, 16) + "..."}
                  size="small"
                  variant="outlined"
                  sx={{ color: "#6c7293", borderColor: "#2c2e33" }}
                />
              </Stack>
              <Typography variant="caption" sx={{ color: "#6c7293" }}>
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
