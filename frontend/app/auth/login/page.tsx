"use client";

import { useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { apiGet, apiPost } from "@/lib/api";
import { isAxiosError } from "axios";

type Step = "credentials" | "mfa";

export default function AuthPage() {
  const [step, setStep] = useState<Step>("credentials");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [otpCode, setOtpCode] = useState("");
  const [factorId, setFactorId] = useState<string | null>(null);
  const [mfaToken, setMfaToken] = useState<string | null>(null);
  const [challengeId, setChallengeId] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();
  const searchParams = useSearchParams();
  const rawRedirectTo = searchParams?.get("next");
  const infoMessage = searchParams?.get("message") || null;

  const getSafeRedirect = (url: string | null): string => {
    if (!url) return "/products";

    const isSafe = url.startsWith("/") && !url.startsWith("//");
    return isSafe ? url : "/products";
  };

  const redirectTo = getSafeRedirect(rawRedirectTo);

  const handleCredentials = async (e: React.FormEvent) => {
      e.preventDefault();
      setError(null);
      setLoading(true);

      try {
          const loginResponse = await apiPost<{
              mfaRequired: boolean;
              factorId: string;
              mfaToken: string;  // <-- token limitado
          }>("/auth/login", { email, password });

          if (loginResponse.mfaRequired && loginResponse.factorId) {
              const challenge = await apiPost<{ id: string }>(
                  "/auth/mfa/challenge",
                  { factorId: loginResponse.factorId },
                  { headers: { "X-MFA-Token": loginResponse.mfaToken } }  // <-- header dedicado
              );
              setFactorId(loginResponse.factorId);
              setChallengeId(challenge.id);
              setMfaToken(loginResponse.mfaToken);  // <-- guarda só o mfaToken
              setStep("mfa");
          } else {
              await redirectByRole();
          }
      } catch {
          setError("Invalid credentials. Please try again.");
      } finally {
          setLoading(false);
      }
  };

  const handleMfa = async (e: React.FormEvent) => {
      e.preventDefault();
      setError(null);
      setLoading(true);

      try {
          await apiPost(
              "/auth/mfa/challenge/verify",
              { factorId, challengeId, code: otpCode },
              { headers: { "X-MFA-Token": mfaToken } }  // <-- header dedicado
          );
          await redirectByRole();
      } catch (err) {
          if (isAxiosError(err) && err.response?.status === 429) {
              setError("Too many attempts. Please wait before trying again.");
          } else {
              setError("Verification failed. Please try again.");
          }
          setOtpCode("");
      } finally {
          setLoading(false);
      }
  };

  const redirectByRole = async () => {
    const me = await apiGet<{ role: string }>("/auth/me");
    router.refresh();
    if (me.role === "MANAGER") {
      router.push("/manager/dashboard");
    } else if (me.role === "CARRIER") {
      router.push("/carrier");
    } else {
      router.push(redirectTo);
    }
    router.refresh();
  };

  return (
    <div className="min-h-[60vh] flex items-center justify-center">
      <div className="w-full max-w-md bg-slate-800 rounded-lg p-8 border border-slate-700">
        {step === "credentials" ? (
          <>
            <h1 className="text-2xl font-bold text-white mb-6">Sign In</h1>

            {infoMessage && (
              <div className="mb-4 p-3 bg-blue-900/60 border border-blue-700 rounded text-blue-200 text-sm">
                {infoMessage}
              </div>
            )}

            <form onSubmit={handleCredentials} className="space-y-4">
              <div>
                <label
                  className="block text-sm text-slate-300 mb-1"
                  htmlFor="email"
                >
                  Email
                </label>
                <input
                  id="email"
                  type="email"
                  autoComplete="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  disabled={loading}
                  className="w-full px-3 py-2 rounded bg-slate-700 text-white placeholder-slate-400 border border-slate-600 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
                />
              </div>

              <div>
                <div className="flex items-center justify-between mb-1">
                  <label className="block text-sm text-slate-300" htmlFor="password">
                    Password
                  </label>
                  <a
                    href="/app/auth/forgot-password"
                    className="text-sm text-blue-400 hover:underline"
                  >
                    Forgot password?
                  </a>
                </div>
                <input
                  id="password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  disabled={loading}
                  className="w-full px-3 py-2 rounded bg-slate-700 text-white placeholder-slate-400 border border-slate-600 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
                />
              </div>

              {error && (
                <p role="alert" className="text-red-400 text-sm">
                  {error}
                </p>
              )}

              <button
                type="submit"
                disabled={loading}
                className="w-full px-4 py-2.5 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded font-semibold hover:from-blue-600 hover:to-blue-700 disabled:opacity-50 transition flex items-center justify-center gap-2"
              >
                {loading ? (
                  <>
                    <span className="inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    Signing in…
                  </>
                ) : (
                  "Sign In"
                )}
              </button>
            </form>
          </>
        ) : (
          <>
            <h1 className="text-2xl font-bold text-white mb-2">
              Two-Factor Authentication
            </h1>
            <p className="text-slate-400 text-sm mb-6">
              Enter the 6-digit code from your authenticator app.
            </p>

            <form onSubmit={handleMfa} className="space-y-4">
              <div>
                <label
                  className="block text-sm text-slate-300 mb-1"
                  htmlFor="otp"
                >
                  Authentication Code
                </label>
                <input
                  id="otp"
                  type="text"
                  inputMode="numeric"
                  autoComplete="one-time-code"
                  maxLength={6}
                  value={otpCode}
                  onChange={(e) =>
                    setOtpCode(e.target.value.replace(/\D/g, ""))
                  }
                  required
                  disabled={loading}
                  placeholder="000000"
                  className="w-full px-3 py-2 rounded bg-slate-700 text-white placeholder-slate-400 border border-slate-600 focus:outline-none focus:ring-2 focus:ring-blue-500 tracking-widest text-center text-xl disabled:opacity-50"
                />
              </div>

              {error && (
                <p role="alert" className="text-red-400 text-sm">
                  {error}
                </p>
              )}

              <button
                type="submit"
                disabled={loading || otpCode.length !== 6}
                className="w-full px-4 py-2.5 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded font-semibold hover:from-blue-600 hover:to-blue-700 disabled:opacity-50 transition flex items-center justify-center gap-2"
              >
                {loading ? (
                  <>
                    <span className="inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    Verifying…
                  </>
                ) : (
                  "Verify"
                )}
              </button>

              <button
                type="button"
                onClick={() => {
                  setStep("credentials");
                  setError(null);
                  setOtpCode("");
                }}
                className="w-full text-sm text-slate-400 hover:text-white transition"
              >
                ← Back to sign in
              </button>
            </form>
          </>
        )}
      </div>
    </div>
  );
}
