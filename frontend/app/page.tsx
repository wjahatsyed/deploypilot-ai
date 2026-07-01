"use client";

import { useEffect, useMemo, useState } from "react";
import { AsyncState } from "@/components/AsyncState";
import { PageHeader } from "@/components/PageHeader";
import { api, backendBaseUrl } from "@/lib/api";
import type { Approval, Customer, EvaluationSummary, Workflow, WorkflowRun } from "@/lib/types";

type DashboardData = {
  customers: Customer[];
  workflows: Workflow[];
  runs: WorkflowRun[];
  approvals: Approval[];
  evalSummary: EvaluationSummary | null;
  runWarning?: string;
  evalWarning?: string;
};

export default function DashboardPage() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError(null);
      try {
        const [customers, workflows, approvals] = await Promise.all([
          api.customers.list(),
          api.workflows.list(),
          api.approvals.pending().catch(() => [] as Approval[])
        ]);

        let runs: WorkflowRun[] = [];
        let runWarning: string | undefined;
        try {
          const runGroups = await Promise.all(workflows.map((workflow) => api.workflows.runs(workflow.id)));
          runs = runGroups.flat();
        } catch {
          runWarning = "Run totals need the backend workflow-run endpoints to be available.";
        }

        let evalSummary: EvaluationSummary | null = null;
        let evalWarning: string | undefined;
        try {
          evalSummary = await api.evals.summary();
        } catch {
          evalWarning = "Evaluation summary is not available from the backend yet.";
        }

        setData({ customers, workflows, runs, approvals, evalSummary, runWarning, evalWarning });
      } catch (err) {
        setError(err instanceof Error ? err.message : "Unable to load dashboard data.");
      } finally {
        setLoading(false);
      }
    }

    load();
  }, []);

  const passRate = useMemo(() => {
    if (!data?.evalSummary) {
      return "Unavailable";
    }
    return `${Math.round(data.evalSummary.passRate * 100)}%`;
  }, [data]);

  return (
    <>
      <PageHeader
        eyebrow="Command center"
        title="Dashboard"
        description={`Connected to ${backendBaseUrl()}. Key operating metrics are pulled from the backend when endpoints are available.`}
      />
      <AsyncState loading={loading} error={error}>
        {data && (
          <>
            {(data.runWarning || data.evalWarning) && (
              <div className="alert info">{[data.runWarning, data.evalWarning].filter(Boolean).join(" ")}</div>
            )}
            <section className="grid statsGrid">
              <div className="stat">
                <span>Total customers</span>
                <strong>{data.customers.length}</strong>
              </div>
              <div className="stat">
                <span>Total workflows</span>
                <strong>{data.workflows.length}</strong>
              </div>
              <div className="stat">
                <span>Total runs</span>
                <strong>{data.runs.length}</strong>
              </div>
              <div className="stat">
                <span>Pending approvals</span>
                <strong>{data.approvals.length}</strong>
              </div>
              <div className="stat">
                <span>Eval pass rate</span>
                <strong>{passRate}</strong>
              </div>
            </section>
          </>
        )}
      </AsyncState>
    </>
  );
}
