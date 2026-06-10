"use client";

import { useState } from "react";
import { useRouter,useSearchParams } from "next/navigation";
import { apiGet, apiPost } from "@/lib/api";
import { isAxiosError } from "axios"

type Step = "credentials" | "mfa";
 
interface MfaFactor {
  id: string;
  factor_type: string;
  status: string;
}


export default function AuthPage() {
  const [step, setStep]         = useState<Step>("credentials");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [otpCode, setOtpCode]   = useState("");
  const [factorId, setFactorId] = useState<string | null>(null);
  const [challengeId, setChallengeId] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();
  const searchParams = useSearchParams();
  const redirectTo = searchParams?.get("next") || "/products";
  const infoMessage = searchParams?.get("message") || null;

  const handleCredentials = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
 
     try {
      await apiPost("/auth/login", { email, password });
    } catch {
      setError("Invalid credentials. Please try again.");
      setLoading(false);
      return;
    }

 
    try {
      const status = await apiGet<{ factors: MfaFactor[] }>("/auth/mfa/status");
      const activeFactor = status.factors?.find(
        (f) => f.factor_type === "totp" && f.status === "verified"
      );
      console.log("Active factor:", activeFactor);
      if (activeFactor) {
        const challenge = await apiPost<{ id: string }>(
          "/auth/mfa/challenge",
          { factorId: activeFactor.id }
        );
        setFactorId(activeFactor.id);
        setChallengeId(challenge.id);
        setStep("mfa");
      } else {
        await redirectByRole();
      }
    } catch {
      setError("Could not verify account security. Please try again.");
    } finally {
      setLoading(false);
    }
  };
  const handleMfa = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
 
    try {
      await apiPost("/auth/mfa/challenge/verify", {
        factorId,
        challengeId,
        code: otpCode,
      });
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
    if (me.role === "MANAGER"){
      router.push("/manager/dashboard");
      router.refresh();
    }     
    else if (me.role === "CARRIER")
      { 
        router.push("/carrier");
        router.refresh();
      }
    else  {
      router.push(redirectTo);
      router.refresh();
    }                          
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
                <label className="block text-sm text-slate-300 mb-1" htmlFor="email">
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
                <label className="block text-sm text-slate-300 mb-1">
                  Password
                </label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  disabled={loading}
                  className="w-full px-3 py-2 rounded bg-slate-700 text-white placeholder-slate-400 border border-slate-600 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
                />
              </div>
 
              {error && (
                <p role="alert" className="text-red-400 text-sm">{error}</p>
              )}
 
              <button
                type="submit"
                disabled={loading}
                className="w-full px-4 py-2.5 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded font-semibold hover:from-blue-600 hover:to-blue-700 disabled:opacity-50 transition flex items-center justify-center gap-2"
              >
                {loading ? (
                  <><span className="inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />Signing in…</>
                ) : "Sign In"}
              </button>
            </form>
 
            <p className="mt-4 text-sm text-slate-400">
              Don&apos;t have an account?{" "}
              <a href="/auth/register" className="text-blue-400 hover:underline">Register</a>
            </p>
            <p className="mt-2 text-sm text-slate-400">
              <a href="/auth/reset-password" className="text-blue-400 hover:underline">Forgot password?</a>
            </p>
          </>
        ) : (
          <>
            <h1 className="text-2xl font-bold text-white mb-2">Two-Factor Authentication</h1>
            <p className="text-slate-400 text-sm mb-6">
              Enter the 6-digit code from your authenticator app.
            </p>
 
            <form onSubmit={handleMfa} className="space-y-4">
              <div>
                <label className="block text-sm text-slate-300 mb-1" htmlFor="otp">
                  Authentication Code
                </label>
                <input
                  id="otp"
                  type="text"
                  inputMode="numeric"
                  autoComplete="one-time-code"
                  maxLength={6}
                  value={otpCode}
                  onChange={(e) => setOtpCode(e.target.value.replace(/\D/g, ""))}
                  required
                  disabled={loading}
                  placeholder="000000"
                  className="w-full px-3 py-2 rounded bg-slate-700 text-white placeholder-slate-400 border border-slate-600 focus:outline-none focus:ring-2 focus:ring-blue-500 tracking-widest text-center text-xl disabled:opacity-50"
                />
              </div>
 
              {error && (
                <p role="alert" className="text-red-400 text-sm">{error}</p>
              )}
 
              <button
                type="submit"
                disabled={loading || otpCode.length !== 6}
                className="w-full px-4 py-2.5 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded font-semibold hover:from-blue-600 hover:to-blue-700 disabled:opacity-50 transition flex items-center justify-center gap-2"
              >
                {loading ? (
                  <><span className="inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />Verifying…</>
                ) : "Verify"}
              </button>
 
              <button
                type="button"
                onClick={() => { setStep("credentials"); setError(null); setOtpCode(""); }}
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
 