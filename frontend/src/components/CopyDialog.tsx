import { useState, useEffect } from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  TextField,
} from "@mui/material";
import { useCopyFile } from "../hooks/useFileStore";
import type { FileMetadata } from "../generated/filestore/v1/file_store_pb";

interface CopyDialogProps {
  file: FileMetadata | null;
  onClose: () => void;
}

export default function CopyDialog({ file, onClose }: CopyDialogProps) {
  const [destinationFilename, setDestinationFilename] = useState("");
  const copyMutation = useCopyFile();

  useEffect(() => {
    if (file) {
      const ext = file.filename.includes(".") ? "." + file.filename.split(".").pop() : "";
      const baseName = file.filename.replace(ext, "");
      setDestinationFilename(`${baseName}-copy${ext}`);
    }
  }, [file]);

  const handleCopy = () => {
    if (!file || !destinationFilename.trim()) return;

    copyMutation.mutate(
      {
        sourceFileId: file.fileId,
        destinationFilename: destinationFilename.trim(),
        sourceVersion: 0,
      },
      {
        onSuccess: () => onClose(),
      },
    );
  };

  return (
    <Dialog open={!!file} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Copy File</DialogTitle>
      <DialogContent>
        <DialogContentText sx={{ mb: 2 }}>
          Create a copy of <strong>{file?.filename}</strong> with a new filename.
        </DialogContentText>
        <TextField
          autoFocus
          fullWidth
          label="Destination Filename"
          value={destinationFilename}
          onChange={(e) => setDestinationFilename(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") handleCopy();
          }}
          error={!destinationFilename.trim()}
          helperText={!destinationFilename.trim() ? "Filename is required" : ""}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button
          onClick={handleCopy}
          variant="contained"
          disabled={!destinationFilename.trim() || copyMutation.isPending}
        >
          {copyMutation.isPending ? "Copying..." : "Copy"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
