import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from "@mui/material";
import type { FileMetadata } from "../generated/filestore/v1/file_store_pb";

interface DeleteDialogProps {
  file: FileMetadata | null;
  onClose: () => void;
  onConfirm: (fileId: string) => void;
}

export default function DeleteDialog({ file, onClose, onConfirm }: DeleteDialogProps) {
  return (
    <Dialog open={!!file} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Delete File</DialogTitle>
      <DialogContent>
        <DialogContentText>
          Are you sure you want to delete <strong>{file?.filename}</strong>? This action will
          soft-delete the file and it will no longer appear in search results.
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button
          onClick={() => {
            if (file) onConfirm(file.fileId);
          }}
          variant="contained"
          color="error"
        >
          Delete
        </Button>
      </DialogActions>
    </Dialog>
  );
}
