export interface ClientApiError {
  status?: number;
  code?: string;
  message: string;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

export function toApiError(reason: unknown): ClientApiError {
  const result = isRecord(reason) ? reason : {};
  const status = typeof result.status === "number" ? result.status : undefined;
  const body = isRecord(result.data) ? result.data : {};
  const backendError = isRecord(body.error) ? body.error : null;

  if (backendError) {
    return {
      status,
      code:
        typeof backendError.code === "string"
          ? backendError.code
          : undefined,
      message:
        typeof backendError.message === "string"
          ? backendError.message
          : "The request failed.",
    };
  }

  if (result.status === "FETCH_ERROR") {
    return {
      code: "NETWORK_ERROR",
      message: "Cannot reach the server. Please try again.",
    };
  }

  return {
    status,
    code: "UNKNOWN_ERROR",
    message: "Something went wrong. Please try again.",
  };
}