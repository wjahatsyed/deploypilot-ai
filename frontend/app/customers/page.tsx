"use client";

import { FormEvent, useEffect, useState } from "react";
import { AsyncState } from "@/components/AsyncState";
import { PageHeader } from "@/components/PageHeader";
import { api } from "@/lib/api";
import type { Customer } from "@/lib/types";

export default function CustomersPage() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [form, setForm] = useState({ name: "", industry: "", region: "", contactEmail: "" });

  async function loadCustomers() {
    setLoading(true);
    setError(null);
    try {
      setCustomers(await api.customers.list());
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to load customers.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadCustomers();
  }, []);

  async function createCustomer(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      await api.customers.create(form);
      setForm({ name: "", industry: "", region: "", contactEmail: "" });
      await loadCustomers();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to create customer.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="Accounts"
        title="Customers"
        description="Create customers and review the customer list returned by the backend."
      />
      {error && <div className="alert error">{error}</div>}
      <div className="grid twoColumn">
        <section className="section">
          <div className="sectionHeader">
            <h3>Create customer</h3>
          </div>
          <form className="form" onSubmit={createCustomer}>
            <label>
              Name
              <input
                required
                value={form.name}
                onChange={(event) => setForm({ ...form, name: event.target.value })}
                placeholder="Acme Operations"
              />
            </label>
            <label>
              Industry
              <input
                value={form.industry}
                onChange={(event) => setForm({ ...form, industry: event.target.value })}
                placeholder="SaaS"
              />
            </label>
            <label>
              Region
              <input
                value={form.region}
                onChange={(event) => setForm({ ...form, region: event.target.value })}
                placeholder="North America"
              />
            </label>
            <label>
              Contact email
              <input
                type="email"
                value={form.contactEmail}
                onChange={(event) => setForm({ ...form, contactEmail: event.target.value })}
                placeholder="ops@example.com"
              />
            </label>
            <button className="button" disabled={saving}>
              {saving ? "Creating..." : "Create customer"}
            </button>
          </form>
        </section>
        <section className="section">
          <div className="sectionHeader">
            <h3>Customer list</h3>
          </div>
          <AsyncState loading={loading} error={null}>
            {customers.length === 0 ? (
              <div className="empty">No customers yet.</div>
            ) : (
              <table className="table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Industry</th>
                    <th>Region</th>
                    <th>Email</th>
                  </tr>
                </thead>
                <tbody>
                  {customers.map((customer) => (
                    <tr key={customer.id}>
                      <td>{customer.name}</td>
                      <td>{customer.industry || "Not set"}</td>
                      <td>{customer.region || "Not set"}</td>
                      <td>{customer.contactEmail || "Not set"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </AsyncState>
        </section>
      </div>
    </>
  );
}
