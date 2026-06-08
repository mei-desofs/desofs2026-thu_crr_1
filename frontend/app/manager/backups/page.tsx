"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";
import { apiPost } from "@/lib/api";

interface LogLine {
  id: number;
  text: string;
  type: "info" | "success" | "error" | "muted" | "separator" | "command";
}

type PageStatus = "idle" | "running";

function timestamp(): string {
  return new Date().toLocaleTimeString("pt-PT", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
}

function getErrorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    return err.response?.data?.message ?? "Request could not be processed.";
  }
  return "Request could not be processed.";
}

// ─── Quick commands ───────────────────────────────────────────────────────────

const QUICK_COMMANDS = [
  {
    command: "./backup_products.sh",
    description: "Creates a new backup of all products.",
  },
  {
    command: "./cleanup_product_backups.sh <YYYY-MM-DD>",
    description:
      "Deletes backups older than the specified date. Example: 2025-01-01",
  },
];

// ─── Component ────────────────────────────────────────────────────────────────

let lineIdCounter = 0;
function nextId() {
  return ++lineIdCounter;
}

export default function BackupConsolePage() {
  const router = useRouter();
  const inputRef = useRef<HTMLInputElement>(null);
  const logEndRef = useRef<HTMLDivElement>(null);

  const [command, setCommand] = useState("");
  const [status, setStatus] = useState<PageStatus>("idle");
  const [lines, setLines] = useState<LogLine[]>([
    {
      id: nextId(),
      text: "Ready. Write a command below and press Enter or click Run.",
      type: "success",
    },
  ]);

  useEffect(() => {
    logEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [lines]);

  const appendLine = (text: string, type: LogLine["type"]) => {
    setLines((prev) => [...prev, { id: nextId(), text, type }]);
  };

  const appendSeparator = () => {
    setLines((prev) => [...prev, { id: nextId(), text: "", type: "separator" }]);
  };

  const clearLog = () => {
    setLines([{ id: nextId(), text: "Log cleared.", type: "muted" }]);
  };

  const handleRun = async () => {
    const cmd = command.trim();
    if (!cmd || status === "running") return;

    setCommand("");
    setStatus("running");

    appendSeparator();
    appendLine(`[${timestamp()}] $ ${cmd}`, "command");
    appendLine("Running...", "muted");

    try {
      const result = await apiPost<string>(
        `/backup/products?command=${encodeURIComponent(cmd)}`
      );

      setLines((prev) => prev.filter((l) => l.text !== "Running..."));

      if (result && result.trim()) {
        appendLine(result.trim(), "success");
      } else {
        appendLine("Completed successfully (no output).", "success");
      }
    } catch (err) {
      setLines((prev) => prev.filter((l) => l.text !== "Running..."));
      appendLine(`Error: ${getErrorMessage(err)}`, "error");
    } finally {
      appendLine(`[${timestamp()}] done`, "muted");
      setStatus("idle");
      setTimeout(() => inputRef.current?.focus(), 50);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") handleRun();
  };

  const isRunning = status === "running";

  return (
    <div className="py-8">
      <main className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">

        {/* Header */}
        <div className="mb-8">
          <button
            onClick={() => router.push("/manager/dashboard")}
            className="flex items-center gap-2 text-slate-400 hover:text-white transition text-sm mb-4"
          >
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
            </svg>
            Back to Dashboard
          </button>
          <h1 className="text-4xl font-bold text-white mb-1">Backup Console</h1>
          <p className="text-slate-400">
            Execute backup scripts on the server. Only files{" "}
            <code className="text-slate-300 bg-slate-700 px-1.5 py-0.5 rounded text-xs">.sh</code>{" "}
            within the directory{" "}
            <code className="text-slate-300 bg-slate-700 px-1.5 py-0.5 rounded text-xs">scripts/</code>{" "}
            are allowed.
          </p>
        </div>

        {/* Console card */}
        <div className="bg-slate-800 rounded-xl border border-slate-700 overflow-hidden">

          {/* Terminal header bar */}
          <div className="flex items-center justify-between px-4 py-3 border-b border-slate-700 bg-slate-800/80">
            <div className="flex items-center gap-2">
              {/* macOS-style dots */}
              <span className="w-3 h-3 rounded-full bg-red-500/70" />
              <span className="w-3 h-3 rounded-full bg-yellow-500/70" />
              <span className="w-3 h-3 rounded-full bg-green-500/70" />
              <span className="ml-2 text-xs text-slate-500 font-mono">backup@techstore ~ /scripts</span>
            </div>
            <button
              onClick={clearLog}
              disabled={isRunning}
              className="flex items-center gap-1.5 text-xs text-slate-500 hover:text-slate-300 transition disabled:opacity-40"
            >
              <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0" />
              </svg>
              Clear
            </button>
          </div>

          {/* Log area */}
          <div className="h-72 overflow-y-auto bg-[#0d1117] px-4 py-4 font-mono text-sm leading-relaxed">
            {lines.map((line) => {
              if (line.type === "separator") {
                return (
                  <div key={line.id} className="border-t border-slate-700/50 my-2" />
                );
              }
              return (
                <div
                  key={line.id}
                  className={
                    line.type === "success"
                      ? "text-green-400"
                      : line.type === "error"
                      ? "text-red-400"
                      : line.type === "command"
                      ? "text-slate-300"
                      : line.type === "muted"
                      ? "text-slate-600"
                      : "text-slate-400"
                  }
                  style={{ whiteSpace: "pre-wrap", wordBreak: "break-all" }}
                >
                  {line.text}
                </div>
              );
            })}
            {/* Running indicator */}
            {isRunning && (
              <span className="inline-block w-2 h-4 bg-green-400 animate-pulse ml-0.5" />
            )}
            <div ref={logEndRef} />
          </div>

          {/* Input row */}
          <div className="flex items-center gap-2 px-4 py-3 border-t border-slate-700 bg-slate-900">
            <span className="text-green-400 font-mono text-sm flex-shrink-0">$</span>
            <input
              ref={inputRef}
              type="text"
              value={command}
              onChange={(e) => setCommand(e.target.value)}
              onKeyDown={handleKeyDown}
              disabled={isRunning}
              placeholder="ex: backup_products.sh --full"
              autoComplete="off"
              spellCheck={false}
              className="flex-1 bg-transparent font-mono text-sm text-white placeholder-slate-600 focus:outline-none disabled:opacity-50"
            />
            <button
              onClick={handleRun}
              disabled={isRunning || !command.trim()}
              className="flex items-center gap-1.5 px-3 py-1.5 bg-blue-600 text-white text-xs font-semibold rounded hover:bg-blue-700 transition disabled:opacity-40 disabled:cursor-not-allowed flex-shrink-0"
            >
              {isRunning ? (
                <>
                  <span className="inline-block w-3 h-3 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Running...
                </>
              ) : (
                <>
                  <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M8 5v14l11-7z" />
                  </svg>
                  Run
                </>
              )}
            </button>
          </div>
        </div>

        {/* Quick commands */}
        <div className="mt-4">
          <p className="text-xs text-slate-500 mb-2">Fast Commands</p>
          <div className="flex flex-wrap gap-2">
            {QUICK_COMMANDS.map((item) => (
              <button
                key={item.command}
                onClick={() => {
                  setCommand(item.command);
                  inputRef.current?.focus();
                }}
                disabled={isRunning}
                className="text-left px-3 py-2 bg-slate-800 border border-slate-700 rounded hover:border-slate-500 transition disabled:opacity-40"
              >
                <div className="font-mono text-xs text-slate-300">
                  {item.command}
                </div>
                <div className="text-[11px] text-slate-500 mt-1">
                  {item.description}
                </div>
              </button>
            ))}
          </div>
        </div>

      </main>
    </div>
  );
}