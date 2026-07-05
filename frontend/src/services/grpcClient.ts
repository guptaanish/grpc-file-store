import { createClient } from "@connectrpc/connect";
import { createGrpcWebTransport } from "@connectrpc/connect-web";
import { FileStoreService } from "../generated/filestore/v1/file_store_pb";

/**
 * gRPC-Web transport configured to talk to the Envoy proxy.
 * In development, Envoy runs at localhost:8081.
 */
const transport = createGrpcWebTransport({
  baseUrl: "http://localhost:8081",
});

/**
 * Typed gRPC-Web client for the FileStoreService.
 * Supports all unary and server-streaming RPCs.
 * Client-streaming RPCs (UploadFile, ResumeUpload) are handled via REST.
 */
export const fileStoreClient = createClient(FileStoreService, transport);

/**
 * Base URL for the REST API endpoints (uploads and downloads).
 * In development, Vite proxies /api to localhost:8080.
 */
export const API_BASE_URL = "/api/v1/files";
