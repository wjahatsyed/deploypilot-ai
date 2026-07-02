"use client";

import { FormEvent, useEffect, useState } from "react";
import { AsyncState } from "@/components/AsyncState";
import { PageHeader } from "@/components/PageHeader";
import { StatusPill } from "@/components/StatusPill";
import { api } from "@/lib/api";
import type { Customer, Workflow, WorkflowRun } from "@/lib/types";

export default function WorkflowsPage() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [workflows, setWorkflows] = useState<Workflow[]>([]);
  const [latestRun, setLatestRun] = useState<WorkflowRun | null>(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [workflowForm, setWorkflowForm] = useState({ customerId: "", name: "", description: "" });
  const [runForm, setRunForm] = useState({ workflowId: "", inputSource: "manual", inputContent: "" });

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const [customerData, workflowData] = await Promise.all([api.customers.list(), api.workflows.list()]);
      setCustomers(customerData);
      setWorkflows(workflowData);
      setWorkflowForm((current) => ({ ...current, customerId: current.customerId || customerData[0]?.id || "" }));
      setRunForm((current) => ({ ...current, workflowId: current.workflowId || workflowData[0]?.id || "" }));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to load workflow data.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function createWorkflow(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await api.workflows.create(workflowForm);
      setWorkflowForm({ customerId: workflowForm.customerId, name: "", description: "" });
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to create workflow.");
    } finally {
      setBusy(false);
    }
  }

  async function runWorkflow(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setError(null);
    setLatestRun(null);
    try {
      const run = await api.workflows.run(runForm.workflowId, {
        inputSource: runForm.inputSource,
        inputContent: runForm.inputContent
      });
      setLatestRun(run);
      setRunForm({ ...runForm, inputContent: "" });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to run workflow.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="Automation"
        title="Workflows"
        description="Create customer workflows, send text into a workflow run, and inspect the backend response."
      />
      {error && <div className="alert error">{error}</div>}
      <AsyncState loading={loading} error={null}>
        <div className="grid twoColumn">
          <section className="section">
            <div className="sectionHeader">
              <h3>Create workflow</h3>
            </div>
            <form className="form" onSubmit={createWorkflow}>
              <label>
                Customer
                <select
                  required
                  value={workflowForm.customerId}
                  onChange={(event) => setWorkflowForm({ ...workflowForm, customerId: event.target.value })}
                >
                  <option value="">Select customer</option>
                  {customers.map((customer) => (
                    <option key={customer.id} value={customer.id}>
                      {customer.name}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Workflow name
                <input
                  required
                  value={workflowForm.name}
                  onChange={(event) => setWorkflowForm({ ...workflowForm, name: event.target.value })}
                  placeholder="Deployment intake"
                />
              </label>
              <label>
                Description
                <textarea
                  value={workflowForm.description}
                  onChange={(event) => setWorkflowForm({ ...workflowForm, description: event.target.value })}
                  placeholder="Classify, extract, recommend, and request approvals for deployment requests."
                />
              </label>
              <button className="button" disabled={busy || customers.length === 0}>
                Create workflow
              </button>
              {customers.length === 0 && <p className="muted">Create a customer before creating workflows.</p>}
            </form>
          </section>
          <section className="section">
            <div className="sectionHeader">
              <h3>Run workflow</h3>
            </div>
            <form className="form" onSubmit={runWorkflow}>
              <label>
                Workflow
                <select
                  required
                  value={runForm.workflowId}
                  onChange={(event) => setRunForm({ ...runForm, workflowId: event.target.value })}
                >
                  <option value="">Select workflow</option>
                  {workflows.map((workflow) => (
                    <option key={workflow.id} value={workflow.id}>
                      {workflow.name}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Input source
                <input
                  value={runForm.inputSource}
                  onChange={(event) => setRunForm({ ...runForm, inputSource: event.target.value })}
                  placeholder="manual"
                />
              </label>
              <label>
                Input text
                <textarea
                  required
                  value={runForm.inputContent}
                  onChange={(event) => setRunForm({ ...runForm, inputContent: event.target.value })}
                  placeholder="Please deploy release 1.2.3 to production after approval."
                />
              </label>
              <button className="button" disabled={busy || workflows.length === 0}>
                Run workflow
              </button>
              {workflows.length === 0 && <p className="muted">Create a workflow before running one.</p>}
            </form>
          </section>
        </div>
        <section className="section">
          <div className="sectionHeader">
            <h3>Latest run result</h3>
            {latestRun && <StatusPill status={latestRun.status} />}
          </div>
          {!latestRun ? (
            <div className="empty">Run a workflow to see the response.</div>
          ) : (
            <div className="resultPanel">
              <dl>
                <div>
                  <dt>Detected intent</dt>
                  <dd>{latestRun.detectedIntent || "Not returned yet. Backend/AI execution is still pending."}</dd>
                </div>
                <div>
                  <dt>Extracted fields</dt>
                  <dd>{latestRun.extractedFieldsJson || "No extracted fields returned yet."}</dd>
                </div>
                <div>
                  <dt>Recommended action</dt>
                  <dd>{latestRun.recommendedAction || "No recommendation returned yet."}</dd>
                </div>
                <div>
                  <dt>Status</dt>
                  <dd>{latestRun.status}</dd>
                </div>
              </dl>
            </div>
          )}
        </section>
      </AsyncState>
    </>
  );
}
