export type Customer = {
  id: string;
  name: string;
  industry?: string | null;
  region?: string | null;
  contactEmail?: string | null;
  createdAt?: string;
  updatedAt?: string;
};

export type CreateCustomerPayload = {
  name: string;
  industry?: string;
  region?: string;
  contactEmail?: string;
};

export type Workflow = {
  id: string;
  customerId: string;
  name: string;
  description?: string | null;
  status: string;
  createdAt?: string;
  updatedAt?: string;
};

export type CreateWorkflowPayload = {
  customerId: string;
  name: string;
  description?: string;
};

export type WorkflowRun = {
  id: string;
  workflowId: string;
  inputSource?: string | null;
  inputContent: string;
  detectedIntent?: string | null;
  extractedFieldsJson?: string | null;
  recommendedAction?: string | null;
  status: string;
  createdAt?: string;
  updatedAt?: string;
};

export type CreateWorkflowRunPayload = {
  inputSource?: string;
  inputContent: string;
};

export type Approval = {
  id: string;
  workflowRunId: string;
  status: string;
  reviewerEmail?: string | null;
  reviewerComment?: string | null;
  requestedBy?: string | null;
  reviewedAt?: string | null;
  createdAt?: string;
  updatedAt?: string;
};

export type EvaluationSummary = {
  totalCases: number;
  passedCases: number;
  passRate: number;
  averageScore: number;
};
