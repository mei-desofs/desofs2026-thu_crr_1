"use client";

import { useEffect, useState } from "react";
import { apiGet, apiPost } from "@/lib/api";
import apiClient from "@/lib/api";
import { isAxiosError } from "axios";
import QRCode from "qrcode";


interface MfaFactor {
  id: string;
  factor_type: string;
  status: string; // "unverified" | "verified"
  friendly_name: string;
}

interface EnrollData {
  id: string; // factorId
  totp: {
    qr_code: string; // SVG data URI
    secret: string;
    uri: string;
  };
}

type PageState = "loading" | "disabled" | "enrolling" | "enabled";

export default function MfaSettingsPage() {
  const [qrUrl, setQrUrl] = useState<string>("");
  const [pageState, setPageState]     = useState<PageState>("loading");
  const [enrollData, setEnrollData]   = useState<EnrollData | null>(null);
  const [activeFactor, setActiveFactor] = useState<MfaFactor | null>(null);
  const [otpCode, setOtpCode]         = useState("");
  const [error, setError]             = useState<string | null>(null);
  const [loading, setLoading]         = useState(false);

  useEffect(() => {
    const load = async () => {
      try {
        const status = await apiGet<{ factors: MfaFactor[] }>("/auth/mfa/status");
        const verified = status.factors?.find(
          (f) => f.factor_type === "totp" && f.status === "verified"
        );
        if (verified) {
          setActiveFactor(verified);
          setPageState("enabled");
        } else {
          if (status.factors?.some((f) => f.factor_type === "totp" && f.status === "unverified")) {
            try {
              await apiClient.delete(`/auth/mfa/${status.factors.find((f) => f.factor_type === "totp" && f.status === "unverified")?.id}`);
            } catch (err) {
              console.error("Failed to delete unverified factor", err);
            } finally {
              setEnrollData(null);
              setOtpCode("");
              setError(null);
              setPageState("disabled");
              setLoading(false);
            }
          }
          setPageState("disabled");
        }
      } catch {
        setPageState("disabled");
      }
    };
    load();
  }, []);

  useEffect(() => {
  if (!enrollData?.totp?.uri) return;

  setQrUrl("");

  QRCode.toDataURL(enrollData.totp.uri)
    .then(setQrUrl)
    .catch(console.error);
  }, [enrollData]);

  const handleStartEnroll = async () => {
    setError(null);
    setLoading(true);
    try {
      const data = await apiPost<EnrollData>("/auth/mfa/enroll", {});
      setEnrollData(data);
      setPageState("enrolling");
    } catch {
      setError("Failed to start setup. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmEnroll = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      
      const challenge = await apiPost<{ id: string }>("/auth/mfa/enroll/challenge", {
      factorId: enrollData!.id,
      });
      await apiPost("/auth/mfa/verify", {
        factorId: enrollData!.id,
        challengeId: challenge.id,
        code: otpCode,
      });
      const status = await apiGet<{ factors: MfaFactor[] }>("/auth/mfa/status");
      const verified = status.factors?.find((f) => f.status === "verified");
      setActiveFactor(verified ?? null);
      setEnrollData(null);
      setOtpCode("");
      setPageState("enabled");
    } catch (err) {
      if (isAxiosError(err) && err.response?.status === 429) {
        setError("Too many attempts. Please wait before trying again.");
      } else {
        setError("Invalid code. Please check your authenticator app and try again.");
      }
      setOtpCode("");
    } finally {
      setLoading(false);
    }
  };

  const handleDisable = async () => {
    if (!activeFactor) return;
    setError(null);
    setLoading(true);
    try {
      await apiClient.delete(`/auth/mfa/${activeFactor.id}`);
      setActiveFactor(null);
      setPageState("disabled");
    } catch {
      setError("Failed to disable 2FA. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleCancelEnroll = async () => {
  if (!enrollData) return;

  setLoading(true);
  setError(null);

  try {
    await apiClient.delete(`/auth/mfa/${enrollData.id}`);
  } catch (err) {
    console.error("Failed to delete unverified factor", err);
  } finally {
    setEnrollData(null);
    setOtpCode("");
    setError(null);
    setPageState("disabled");
    setLoading(false);
  }
};

  return (
    <div className="py-8">
      <main className="max-w-lg mx-auto px-4 sm:px-6 lg:px-8">
        <h1 className="text-4xl font-bold text-white mb-2">
          Two-Factor Authentication
        </h1>
        <p className="text-slate-400 mb-8">
          Add an extra layer of security to your account using an authenticator app.
        </p>

        {error && (
          <div className="mb-6 p-4 bg-red-900 border border-red-700 rounded text-red-100 text-sm">
            {error}
          </div>
        )}

        {pageState === "loading" && (
          <div className="text-center py-12">
            <div className="w-10 h-10 border-4 border-slate-700 border-t-blue-500 rounded-full animate-spin mx-auto" />
          </div>
        )}

        {pageState === "disabled" && (
          <div className="bg-slate-800 rounded-lg border border-slate-700 p-6">
            <div className="flex items-center gap-3 mb-4">
              <span className="w-3 h-3 rounded-full bg-slate-500 inline-block" />
              <span className="text-slate-300 font-medium">2FA is disabled</span>
            </div>
            <p className="text-slate-400 text-sm mb-6">
              Use an authenticator app (Google Authenticator, Authy, etc.) to generate
              time-based one-time passwords.
            </p>
            <button
              onClick={handleStartEnroll}
              disabled={loading}
              className="px-5 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50 transition"
            >
              {loading ? "Setting up…" : "Enable 2FA"}
            </button>
          </div>
        )}

        {pageState === "enrolling" && enrollData && (
          <div className="bg-slate-800 rounded-lg border border-slate-700 p-6 space-y-6">
            <div>
              <p className="text-white font-semibold mb-1">
                1. Scan this QR code with your authenticator app
              </p>
              <p className="text-slate-400 text-sm mb-4">
                Or enter the secret manually: {" "}
                <code className="bg-slate-900 px-2 py-0.5 rounded text-slate-200 text-xs tracking-widest">
                  {enrollData.totp.secret}
                </code>
              </p>
              <div className="flex justify-center">
                <img
                  src={qrUrl}
                  className="w-48 h-48 bg-white p-2 rounded"
                  alt="2FA QR Code"
                />
              </div>
            </div>
            <div>
              <p className="text-white font-semibold mb-3">
                2. Enter the 6-digit code to confirm setup
              </p>
              <form onSubmit={handleConfirmEnroll} className="space-y-4">
                <input
                  type="text"
                  inputMode="numeric"
                  autoComplete="one-time-code"
                  maxLength={6}
                  value={otpCode}
                  onChange={(e) => setOtpCode(e.target.value.replace(/\D/g, ""))}
                  placeholder="000000"
                  required
                  disabled={loading}
                  className="w-full px-3 py-2 rounded bg-slate-700 text-white placeholder-slate-400 border border-slate-600 focus:outline-none focus:ring-2 focus:ring-blue-500 tracking-widest text-center text-xl disabled:opacity-50"
                />
                <div className="flex gap-3">
                  <button
                    type="submit"
                    disabled={loading || otpCode.length !== 6}
                    className="flex-1 px-5 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50 transition"
                  >
                    {loading ? "Verifying…" : "Confirm & Enable"}
                  </button>
                  <button
                    type="button"
                    onClick={handleCancelEnroll}
                    disabled={loading}
                    className="px-5 py-2 bg-slate-700 text-white rounded hover:bg-slate-600 disabled:opacity-50 transition"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {pageState === "enabled" && (
          <div className="bg-slate-800 rounded-lg border border-slate-700 p-6">
            <div className="flex items-center gap-3 mb-4">
              <span className="w-3 h-3 rounded-full bg-green-500 inline-block" />
              <span className="text-green-300 font-medium">2FA is enabled</span>
            </div>
            <p className="text-slate-400 text-sm mb-6">
              Your account is protected with two-factor authentication.
              You will be asked for a code each time you sign in.
            </p>
            <button
              onClick={handleDisable}
              disabled={loading}
              className="px-5 py-2 bg-red-800 text-red-100 rounded hover:bg-red-700 disabled:opacity-50 transition"
            >
              {loading ? "Disabling…" : "Disable 2FA"}
            </button>
          </div>
        )}
      </main>
    </div>
  );
}