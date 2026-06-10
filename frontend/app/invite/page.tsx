"use client";

import { useState } from "react";
import { apiPost } from "@/lib/api";
import { useRouter } from "next/navigation";
import axios from "axios";

type Role = "MANAGER" | "CARRIER";

interface InviteSignupRequest {
  email: string;
  role: Role;
}

type FormStatus = "idle" | "loading" | "success" | "error";

function getErrorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    return err.response?.data?.message ?? "Request could not be processed.";
  }
  return "Request could not be processed.";
}

export default function InvitePage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [role, setRole] = useState<Role>("CARRIER");
  const [status, setStatus] = useState<FormStatus>("idle");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const isValidEmail = (value: string) =>
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);

  const handleSubmit = async (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();

    if (!email.trim() || !isValidEmail(email)) {
      setErrorMessage("Please enter a valid email address.");
      setStatus("error");
      return;
    }

    try {
      setStatus("loading");
      setErrorMessage(null);

      const payload: InviteSignupRequest = { email: email.trim(), role };
      await apiPost("/auth/invite", payload);

      setStatus("success");
      setEmail("");
      setRole("CARRIER");
    } catch (err: unknown) {
      setErrorMessage(getErrorMessage(err));
      setStatus("error");
    }
  };

  const handleNewInvite = () => {
    setStatus("idle");
    setErrorMessage(null);
  };

  return (
    <div className="py-8">
      <main className="max-w-lg mx-auto px-4 sm:px-6 lg:px-8">
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
          <h1 className="text-4xl font-bold text-white mb-2">Invite User</h1>
          <p className="text-slate-400">
            Send an invitation to a new team member. This action is restricted
            to Managers.
          </p>
        </div>

        {status === "success" ? (
          <SuccessBanner onNewInvite={handleNewInvite} />
        ) : (
          <div className="bg-slate-800 rounded-lg border border-slate-700 p-6">
            {/* Email */}
            <div className="mb-5">
              <label
                htmlFor="email"
                className="block text-sm font-medium text-slate-300 mb-1.5"
              >
                Email address
              </label>
              <input
                id="email"
                type="email"
                placeholder="colleague@company.com"
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value);
                  if (status === "error") {
                    setStatus("idle");
                    setErrorMessage(null);
                  }
                }}
                disabled={status === "loading"}
                className="w-full px-4 py-2.5 rounded bg-slate-900 text-white placeholder-slate-500 border border-slate-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50 transition"
              />
            </div>

            {/* Role */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-slate-300 mb-1.5">
                Role
              </label>
              <div className="grid grid-cols-2 gap-3">
                <RoleOption
                  value="CARRIER"
                  selected={role === "CARRIER"}
                  onSelect={() => setRole("CARRIER")}
                  label="Carrier"
                  description="Can view and manage shipments"
                  disabled={status === "loading"}
                />
                <RoleOption
                  value="MANAGER"
                  selected={role === "MANAGER"}
                  onSelect={() => setRole("MANAGER")}
                  label="Manager"
                  description="Full access, can invite users"
                  disabled={status === "loading"}
                />
              </div>
            </div>

            {/* Error */}
            {status === "error" && errorMessage && (
              <div className="mb-5 p-3 bg-red-900/60 border border-red-700 rounded text-red-200 text-sm">
                {errorMessage}
              </div>
            )}

            {/* Submit */}
            <button
              onClick={handleSubmit}
              disabled={status === "loading" || !email.trim()}
              className="w-full px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded font-semibold disabled:opacity-50 hover:from-blue-600 hover:to-blue-700 transition flex items-center justify-center gap-2"
            >
              {status === "loading" ? (
                <>
                  <span className="inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Sending invitation...
                </>
              ) : (
                "Send Invitation"
              )}
            </button>
          </div>
        )}
      </main>
    </div>
  );
}

interface RoleOptionProps {
  value: Role;
  selected: boolean;
  onSelect: () => void;
  label: string;
  description: string;
  disabled: boolean;
}

function RoleOption({
  selected,
  onSelect,
  label,
  description,
  disabled,
}: RoleOptionProps) {
  return (
    <button
      onClick={onSelect}
      disabled={disabled}
      className={`text-left p-3 rounded border transition disabled:opacity-50 ${
        selected
          ? "border-blue-500 bg-blue-900/30 ring-1 ring-blue-500"
          : "border-slate-600 bg-slate-900 hover:border-slate-500"
      }`}
    >
      <p
        className={`text-sm font-semibold mb-0.5 ${
          selected ? "text-blue-300" : "text-white"
        }`}
      >
        {label}
      </p>
      <p className="text-xs text-slate-400">{description}</p>
    </button>
  );
}

interface SuccessBannerProps {
  onNewInvite: () => void;
}

function SuccessBanner({ onNewInvite }: SuccessBannerProps) {
  return (
    <div className="bg-slate-800 rounded-lg border border-slate-700 p-8 text-center">
      <div className="w-14 h-14 bg-green-900/50 rounded-full flex items-center justify-center mx-auto mb-4">
        <svg
          className="w-7 h-7 text-green-400"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          strokeWidth={2}
        >
          <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
        </svg>
      </div>
      <h2 className="text-xl font-bold text-white mb-2">Invitation sent!</h2>
      <p className="text-slate-400 mb-6">
        The user will receive an email with instructions to complete their
        registration.
      </p>
      <button
        onClick={onNewInvite}
        className="px-6 py-2.5 bg-blue-600 text-white rounded font-medium hover:bg-blue-700 transition"
      >
        Send another invitation
      </button>
    </div>
  );
}