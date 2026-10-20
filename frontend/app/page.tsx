"use client";

import { useState } from "react";

const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8080/api/v1/pastes";

type CreateResponse = {
  shortId: string;
  content: string;
  createdAt: string;
  deleteToken: string;
};

export default function Home() {
  const [content, setContent] = useState("");
  const [burnAfterRead, setBurnAfterRead] = useState(false);
  const [result, setResult] = useState<CreateResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setResult(null);
    setLoading(true);

    try {
      const res = await fetch(API_BASE, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ content, burnAfterRead }),
      });

      if (!res.ok) {
        throw new Error(`Request failed (${res.status})`);
      }

      setResult(await res.json());
      setContent("");
      setBurnAfterRead(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex flex-1 flex-col items-center bg-zinc-50 font-sans dark:bg-black">
      <main className="flex w-full max-w-2xl flex-1 flex-col gap-8 px-6 py-20 sm:px-10">
        <header className="flex flex-col gap-2">
          <h1 className="text-3xl font-semibold tracking-tight text-zinc-950 dark:text-zinc-50">
            CuraSnip
          </h1>
          <p className="text-zinc-600 dark:text-zinc-400">
            Share a text snippet. It gets a short link you can send anywhere.
          </p>
        </header>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="Paste your text here..."
            rows={10}
            required
            className="w-full resize-y rounded-xl border border-zinc-200 bg-white p-4 font-mono text-sm text-zinc-900 outline-none focus:border-zinc-400 dark:border-zinc-800 dark:bg-zinc-950 dark:text-zinc-100"
          />

          <label className="flex items-center gap-2 text-sm text-zinc-600 dark:text-zinc-400">
            <input
              type="checkbox"
              checked={burnAfterRead}
              onChange={(e) => setBurnAfterRead(e.target.checked)}
              className="h-4 w-4 rounded border-zinc-300 dark:border-zinc-700"
            />
            Burn after read
          </label>

          <button
            type="submit"
            disabled={loading || content.trim().length === 0}
            className="h-11 w-full rounded-full bg-zinc-950 font-medium text-zinc-50 transition-colors hover:bg-zinc-800 disabled:opacity-50 dark:bg-zinc-100 dark:text-zinc-950 dark:hover:bg-zinc-300 sm:w-48"
          >
            {loading ? "Creating..." : "Create paste"}
          </button>
        </form>

        {error && (
          <p className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
            {error}
          </p>
        )}

        {result && (
          <div className="flex flex-col gap-2 rounded-xl border border-zinc-200 bg-white p-4 text-sm dark:border-zinc-800 dark:bg-zinc-950">
            <p className="text-zinc-600 dark:text-zinc-400">
              View link:{" "}
              <a
                href={`/p/${result.shortId}`}
                className="font-mono text-zinc-950 underline dark:text-zinc-50"
              >
                /p/{result.shortId}
              </a>
            </p>
            <p className="text-zinc-600 dark:text-zinc-400">
              Delete token:{" "}
              <span className="font-mono text-zinc-950 dark:text-zinc-50">
                {result.deleteToken}
              </span>
            </p>
          </div>
        )}
      </main>
    </div>
  );
}
