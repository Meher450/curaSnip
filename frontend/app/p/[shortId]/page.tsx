"use client";

import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8080/api/v1/pastes";

type PasteResponse = {
  shortId: string;
  content: string;
  createdAt: string;
};

export default function ViewPaste() {
  const { shortId } = useParams<{ shortId: string }>();
  const [paste, setPaste] = useState<PasteResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const res = await fetch(`${API_BASE}/${shortId}`);

        if (!cancelled) {
          if (!res.ok) {
            setError(
              res.status === 404
                ? "This paste doesn't exist, expired, or was already burned."
                : `Request failed (${res.status})`
            );
          } else {
            setPaste(await res.json());
          }
        }
      } catch {
        if (!cancelled) setError("Could not reach the API.");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, [shortId]);

  return (
    <div className="flex flex-1 flex-col items-center bg-zinc-50 font-sans dark:bg-black">
      <main className="flex w-full max-w-2xl flex-1 flex-col gap-6 px-6 py-20 sm:px-10">
        <a
          href="/"
          className="text-sm text-zinc-500 hover:text-zinc-900 dark:text-zinc-500 dark:hover:text-zinc-100"
        >
          ← New paste
        </a>

        {loading && (
          <p className="text-zinc-600 dark:text-zinc-400">Loading...</p>
        )}

        {error && (
          <p className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
            {error}
          </p>
        )}

        {paste && (
          <div className="flex flex-col gap-4">
            <p className="font-mono text-xs text-zinc-500 dark:text-zinc-500">
              {paste.shortId} · {new Date(paste.createdAt).toLocaleString()}
            </p>
            <pre className="w-full overflow-x-auto whitespace-pre-wrap break-words rounded-xl border border-zinc-200 bg-white p-4 font-mono text-sm text-zinc-900 dark:border-zinc-800 dark:bg-zinc-950 dark:text-zinc-100">
              {paste.content}
            </pre>
          </div>
        )}
      </main>
    </div>
  );
}
