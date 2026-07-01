type StatusPillProps = {
  status?: string | null;
};

export function StatusPill({ status }: StatusPillProps) {
  const normalized = (status ?? "unknown").toLowerCase();
  const tone = normalized.includes("pass") || normalized.includes("active") || normalized.includes("approved")
    ? "green"
    : normalized.includes("pending") || normalized.includes("queued") || normalized.includes("draft")
      ? "amber"
      : "neutral";

  return <span className={`pill ${tone}`}>{status ?? "UNKNOWN"}</span>;
}
