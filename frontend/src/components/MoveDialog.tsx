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
import { useMoveFile } from "../hooks/useFileStore";
import type { FileMetadata } from "../generated/filestore/v1/file_store_pb";

interface MoveDialogProps {
  file: FileMetadata | null;
  onClose: () => void;
}

export default function MoveDialog({ file, onClose }: MoveDialogProps) {
  const [newFilename, setNewFilename] = useState("");
  const moveMutation = useMoveFile();

  useEffect(() => {
    if (file) {
      setNewFilename(file.filename);
    }
  }, [file]);

  const handleMove = () => {
    if (!file || !newFilename.trim()) return;

    moveMutation.mutate(
      {
        fileId: file.fileId,
        newFilename: newFilename.trim(),
      },
      {
        onSuccess: () => onClose(),
      },
    );
  };

  return (
    <Dialog open={!!file} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Rename / Move File</DialogTitle>
      <DialogContent>
        <DialogContentText sx={{ mb: 2 }}>
          Rename <strong>{file?.filename}</strong> to a new filename.
        </DialogContentText>
        <TextField
          autoFocus
          fullWidth
          label="New Filename"
          value={newFilename}
          onChange={(e) => setNewFilename(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") handleMove();
          }}
          error={!newFilename.trim()}
          helperText={!newFilename.trim() ? "Filename is required" : ""}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button
          onClick={handleMove}
          variant="contained"
          disabled={!newFilename.trim() || moveMutation.isPending}
        >
          {moveMutation.isPending ? "Renaming..." : "Rename"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
