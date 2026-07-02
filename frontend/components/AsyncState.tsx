type AsyncStateProps = {
  loading: boolean;
  error?: string | null;
  children: React.ReactNode;
};

export function AsyncState({ loading, error, children }: AsyncStateProps) {
  if (loading) {
    return <div className="alert info">Loading latest data...</div>;
  }

  if (error) {
    return <div className="alert error">{error}</div>;
  }

  return <>{children}</>;
}
