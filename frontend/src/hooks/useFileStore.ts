import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { fileStoreClient, API_BASE_URL } from "../services/grpcClient";
import type { FileMetadata } from "../generated/filestore/v1/file_store_pb";

// ─── Query Keys ──────────────────────────────────────────────────────────────

export const queryKeys = {
  files: (query: string, pageToken: string) => ["files", query, pageToken] as const,
  fileMetadata: (fileId: string) => ["fileMetadata", fileId] as const,
  fileVersions: (fileId: string) => ["fileVersions", fileId] as const,
  uploadStatus: (sessionId: string) => ["uploadStatus", sessionId] as const,
};

// ─── List Files (gRPC-Web) ───────────────────────────────────────────────────

export function useListFiles(searchQuery: string, pageSize: number, pageToken: string) {
  return useQuery({
    queryKey: queryKeys.files(searchQuery, pageToken),
    queryFn: async () => {
      const response = await fileStoreClient.listFiles({
        searchQuery,
        pageSize,
        pageToken,
      });
      return {
        files: response.files as FileMetadata[],
        nextPageToken: response.nextPageToken,
      };
    },
  });
}

// ─── Get File Metadata (gRPC-Web) ────────────────────────────────────────────

export function useFileMetadata(fileId: string | null) {
  return useQuery({
    queryKey: queryKeys.fileMetadata(fileId ?? ""),
    queryFn: async () => {
      const response = await fileStoreClient.getFileMetadata({ fileId: fileId! });
      return response;
    },
    enabled: !!fileId,
  });
}

// ─── Get File Versions (gRPC-Web) ────────────────────────────────────────────

export function useFileVersions(fileId: string | null) {
  return useQuery({
    queryKey: queryKeys.fileVersions(fileId ?? ""),
    queryFn: async () => {
      const response = await fileStoreClient.getFileVersions({ fileId: fileId! });
      return response.versions;
    },
    enabled: !!fileId,
  });
}

// ─── Delete File (gRPC-Web) ──────────────────────────────────────────────────

export function useDeleteFile() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (fileId: string) => {
      return await fileStoreClient.deleteFile({ fileId });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["files"] });
    },
  });
}

// ─── Copy File (gRPC-Web) ────────────────────────────────────────────────────

export function useCopyFile() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (params: {
      sourceFileId: string;
      destinationFilename: string;
      sourceVersion: number;
    }) => {
      return await fileStoreClient.copyFile(params);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["files"] });
    },
  });
}

// ─── Move File (gRPC-Web) ────────────────────────────────────────────────────

export function useMoveFile() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (params: { fileId: string; newFilename: string }) => {
      return await fileStoreClient.moveFile(params);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["files"] });
    },
  });
}

// ─── Upload File (REST) ──────────────────────────────────────────────────────

export interface UploadProgress {
  loaded: number;
  total: number;
  percent: number;
}

export interface UploadResult {
  fileId: string;
  filename: string;
  size: number;
  checksum: string;
  version: number;
}

export function useUploadFile() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({
      file,
      onProgress,
    }: {
      file: File;
      onProgress?: (progress: UploadProgress) => void;
    }): Promise<UploadResult> => {
      const formData = new FormData();
      formData.append("file", file);

      return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open("POST", `${API_BASE_URL}/upload`);

        xhr.upload.addEventListener("progress", (event) => {
          if (event.lengthComputable && onProgress) {
            onProgress({
              loaded: event.loaded,
              total: event.total,
              percent: Math.round((event.loaded / event.total) * 100),
            });
          }
        });

        xhr.addEventListener("load", () => {
          if (xhr.status >= 200 && xhr.status < 300) {
            resolve(JSON.parse(xhr.responseText) as UploadResult);
          } else {
            reject(new Error(`Upload failed: ${xhr.status} ${xhr.statusText}`));
          }
        });

        xhr.addEventListener("error", () => {
          reject(new Error("Upload failed: network error"));
        });

        xhr.send(formData);
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["files"] });
    },
  });
}

// ─── Download File (REST) ────────────────────────────────────────────────────

export function useDownloadFile() {
  return useMutation({
    mutationFn: async ({ fileId, version = 0 }: { fileId: string; version?: number }) => {
      const url = `${API_BASE_URL}/${fileId}/download?version=${version}`;
      const response = await fetch(url);

      if (!response.ok) {
        throw new Error(`Download failed: ${response.status} ${response.statusText}`);
      }

      const disposition = response.headers.get("Content-Disposition");
      const filenameMatch = disposition?.match(/filename="(.+?)"/);
      const filename = filenameMatch?.[1] ?? "download";

      const blob = await response.blob();
      const downloadUrl = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = downloadUrl;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(downloadUrl);
    },
  });
}

// ─── Get Upload Status (gRPC-Web) ───────────────────────────────────────────

export function useUploadStatus(sessionId: string | null) {
  return useQuery({
    queryKey: queryKeys.uploadStatus(sessionId ?? ""),
    queryFn: async () => {
      const response = await fileStoreClient.getUploadStatus({ sessionId: sessionId! });
      return response;
    },
    enabled: !!sessionId,
    refetchInterval: 1000,
  });
}

// ─── Resumable Upload (REST) ─────────────────────────────────────────────────

export interface ResumableUploadSession {
  sessionId: string;
  filename: string;
  bytesReceived: number;
  totalSize: number;
}

export function useInitiateResumableUpload() {
  return useMutation({
    mutationFn: async (params: {
      filename: string;
      contentType: string;
      totalSize: number;
    }): Promise<ResumableUploadSession> => {
      const searchParams = new URLSearchParams({
        filename: params.filename,
        contentType: params.contentType,
        totalSize: String(params.totalSize),
      });

      const response = await fetch(`${API_BASE_URL}/upload/initiate?${searchParams}`, {
        method: "POST",
      });

      if (!response.ok) {
        throw new Error(`Initiate failed: ${response.status}`);
      }

      return (await response.json()) as ResumableUploadSession;
    },
  });
}

export function useResumeUploadChunk() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (params: {
      sessionId: string;
      chunk: Blob;
      offset: number;
    }): Promise<{ bytesReceived: number; totalSize: number; state: string } | UploadResult> => {
      const formData = new FormData();
      formData.append("chunk", params.chunk);

      const response = await fetch(
        `${API_BASE_URL}/upload/${params.sessionId}/resume?offset=${params.offset}`,
        { method: "POST", body: formData },
      );

      if (!response.ok) {
        throw new Error(`Resume failed: ${response.status}`);
      }

      return await response.json();
    },
    onSuccess: (data) => {
      if ("fileId" in data) {
        queryClient.invalidateQueries({ queryKey: ["files"] });
      }
    },
  });
}
