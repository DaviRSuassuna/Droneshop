/** Espelha o formato RFC 7807 (application/problem+json) devolvido pela API. */
export interface ProblemFieldError {
  field: string;
  message: string;
}

export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  codigo?: string;
  instance?: string;
  timestamp?: string;
  traceId?: string;
  errors?: ProblemFieldError[];
}
