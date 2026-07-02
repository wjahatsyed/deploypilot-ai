import type {
  Approval,
  CreateCustomerPayload,
  CreateWorkflowPayload,
  CreateWorkflowRunPayload,
  Customer,
  EvaluationSummary,
  Workflow,
  WorkflowRun
} from "./types";

const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status?: number
  ) {
    super(message);
    this.name = "ApiError";
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${backendUrl}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {})
    },
    cache: "no-store"
  });

  if (!response.ok) {
    const body = await response.text();
    throw new ApiError(body || `Request failed: ${response.status}`, response.status);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export const api = {
  customers: {
    list: () => request<Customer[]>("/api/customers"),
    create: (payload: CreateCustomerPayload) =>
      request<Customer>("/api/customers", {
        method: "POST",
        body: JSON.stringify(payload)
      })
  },
  workflows: {
    list: () => request<Workflow[]>("/api/workflows"),
    create: (payload: CreateWorkflowPayload) =>
      request<Workflow>("/api/workflows", {
        method: "POST",
        body: JSON.stringify(payload)
      }),
    runs: (workflowId: string) => request<WorkflowRun[]>(`/api/workflows/${workflowId}/runs`),
    run: (workflowId: string, payload: CreateWorkflowRunPayload) =>
      request<WorkflowRun>(`/api/workflows/${workflowId}/runs`, {
        method: "POST",
        body: JSON.stringify(payload)
      })
  },
  approvals: {
    pending: () => request<Approval[]>("/api/approvals/pending"),
    approve: (runId: string, email: string, comment: string) =>
      request<Approval>(`/api/runs/${runId}/approve`, {
        method: "POST",
        body: JSON.stringify({ email, comment })
      }),
    reject: (runId: string, email: string, comment: string) =>
      request<Approval>(`/api/runs/${runId}/reject`, {
        method: "POST",
        body: JSON.stringify({ email, comment })
      })
  },
  evals: {
    summary: () => request<EvaluationSummary>("/api/evals/summary")
  }
};

export function backendBaseUrl() {
  return backendUrl;
}
