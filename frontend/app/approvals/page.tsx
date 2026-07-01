"use client";

import { FormEvent, useEffect, useState } from "react";
import { AsyncState } from "@/components/AsyncState";
import { PageHeader } from "@/components/PageHeader";
import { StatusPill } from "@/components/StatusPill";
import { api } from "@/lib/api";
import type { Approval } from "@/lib/types";

export default function ApprovalsPage() {
  const [approvals, setApprovals] = useState<Approval[]>([]);
  const [reviewerEmail, setReviewerEmail] = useState("reviewer@deploypilot.ai");
  const [comment, setComment] = useState("");
  const [loading, setLoading] = useState(true);
  const [busyRunId, setBusyRunId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      setApprovals(await api.approvals.pending());
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to load pending approvals.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function reviewDecision(runId: string, decision: "approve" | "reject") {
    setBusyRunId(runId);
    setError(null);
    try {
      if (decision === "approve") {
        await api.approvals.approve(runId, reviewerEmail, comment);
      } else {
        await api.approvals.reject(runId, reviewerEmail, comment);
      }
      setComment("");
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to submit approval decision.");
    } finally {
      setBusyRunId(null);
    }
  }

  function approve(event: FormEvent<HTMLFormElement>, runId: string) {
    event.preventDefault();
    void reviewDecision(runId, "approve");
  }

  return (
    <>
      <PageHeader
        eyebrow="Human-in-the-loop"
        title="Approvals"
        description="Review pending approvals and send approve or reject decisions to the backend."
      />
      <section className="section">
        <div className="sectionHeader">
          <h3>Reviewer</h3>
        </div>
        <div className="form">
          <label>
            Reviewer email
            <input value={reviewerEmail} onChange={(event) => setReviewerEmail(event.target.value)} />
          </label>
          <label>
            Comment
            <textarea value={comment} onChange={(event) => setComment(event.target.value)} />
          </label>
        </div>
      </section>
      <AsyncState loading={loading} error={error}>
        <section className="section">
          <div className="sectionHeader">
            <h3>Pending approvals</h3>
          </div>
          {approvals.length === 0 ? (
            <div className="empty">No pending approvals returned by the backend.</div>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Run</th>
                  <th>Status</th>
                  <th>Requested by</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {approvals.map((approval) => (
                  <tr key={approval.id}>
                    <td>{approval.workflowRunId}</td>
                    <td>
                      <StatusPill status={approval.status} />
                    </td>
                    <td>{approval.requestedBy || "Not set"}</td>
                    <td>
                      <form className="buttonRow" onSubmit={(event) => approve(event, approval.workflowRunId)}>
                        <button className="button" disabled={busyRunId === approval.workflowRunId || !reviewerEmail}>
                          Approve
                        </button>
                        <button
                          className="button danger"
                          disabled={busyRunId === approval.workflowRunId || !reviewerEmail}
                          type="button"
                          onClick={() => void reviewDecision(approval.workflowRunId, "reject")}
                        >
                          Reject
                        </button>
                      </form>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      </AsyncState>
    </>
  );
}
