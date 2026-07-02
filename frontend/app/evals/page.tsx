"use client";

import { useEffect, useState } from "react";
import { AsyncState } from "@/components/AsyncState";
import { PageHeader } from "@/components/PageHeader";
import { api } from "@/lib/api";
import type { EvaluationSummary } from "@/lib/types";

export default function EvalsPage() {
  const [summary, setSummary] = useState<EvaluationSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError(null);
      try {
        setSummary(await api.evals.summary());
      } catch (err) {
        setError(err instanceof Error ? err.message : "Evaluation summary endpoint is not available.");
      } finally {
        setLoading(false);
      }
    }

    load();
  }, []);

  return (
    <>
      <PageHeader
        eyebrow="Quality"
        title="Evals"
        description="Monitor the latest evaluation summary from the backend evaluation subsystem."
      />
      <AsyncState loading={loading} error={error}>
        <section className="grid statsGrid">
          <div className="stat">
            <span>Total cases</span>
            <strong>{summary?.totalCases ?? 0}</strong>
          </div>
          <div className="stat">
            <span>Passed cases</span>
            <strong>{summary?.passedCases ?? 0}</strong>
          </div>
          <div className="stat">
            <span>Pass rate</span>
            <strong>{summary ? `${Math.round(summary.passRate * 100)}%` : "0%"}</strong>
          </div>
          <div className="stat">
            <span>Average score</span>
            <strong>{summary ? summary.averageScore.toFixed(2) : "0.00"}</strong>
          </div>
          <div className="stat">
            <span>Status</span>
            <strong>{summary && summary.totalCases > 0 ? "Ready" : "No data"}</strong>
          </div>
        </section>
        <section className="section">
          <div className="sectionHeader">
            <h3>Evaluation summary</h3>
          </div>
          <p className="muted">
            {summary && summary.totalCases > 0
              ? "Evaluation data is available from the backend."
              : "No evaluation cases have been returned yet. Create datasets and run evaluations from the backend API when ready."}
          </p>
        </section>
      </AsyncState>
    </>
  );
}
