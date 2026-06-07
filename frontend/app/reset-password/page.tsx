"use client";
 
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";
 
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL;
 
type FormStatus = "idle" | "loading" | "success" | "error";
type FlowType = "invite" | "recovery";
 
interface PasswordStrength {
  score: number;
  label: string;
  color: string;
}
 
function getPasswordStrength(password: string): PasswordStrength {
  if (password.length === 0) return { score: 0, label: "", color: "" };
 
  // Strength based solely on length
  if (password.length < 8)  return { score: 1, label: "Weak",   color: "bg-red-500" };
  if (password.length < 12) return { score: 2, label: "Fair",   color: "bg-orange-400" };
  if (password.length < 16) return { score: 3, label: "Good",   color: "bg-yellow-400" };
                             return { score: 4, label: "Strong", color: "bg-green-500" };
}
 
function getErrorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    return err.response?.data?.message ?? "Request could not be processed.";
  }
  return "Request could not be processed.";
}
 
export default function SetPasswordPage() {
  const router = useRouter();
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [flowType, setFlowType] = useState<FlowType>("invite");
  const [invalidLink, setInvalidLink] = useState(false);
 
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [status, setStatus] = useState<FormStatus>("idle");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
 
  useEffect(() => {
    const hash = window.location.hash.substring(1);
    const params = new URLSearchParams(hash);
    const token = params.get("access_token");
    const type = params.get("type");
 
    if (!token || (type !== "invite" && type !== "recovery")) {
      setInvalidLink(true);
    } else {
      setAccessToken(token);
      setFlowType(type as FlowType);
    }
  }, []);
 
  const strength = getPasswordStrength(password);
  const passwordsMatch = confirmPassword.length > 0 && password === confirmPassword;
  const passwordsMismatch = confirmPassword.length > 0 && password !== confirmPassword;
  const isValid = password.length >= 8 && passwordsMatch;
 
  const handleSubmit = async (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    if (!isValid || !accessToken) return;
 
    try {
      setStatus("loading");
      setErrorMessage(null);
 
      await axios.post(
        `${API_BASE_URL}/auth/set-password`,
        { newPassword: password },
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${accessToken}`,
          },
        }
      );
 
      setStatus("success");
    } catch (err) {
      setErrorMessage(getErrorMessage(err));
      setStatus("error");
    }
  };
 
  if (invalidLink) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <div className="w-full max-w-md bg-slate-800 rounded-lg p-8 border border-slate-700 text-center">
          <div className="w-14 h-14 bg-red-900/50 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-7 h-7 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m9-.75a9 9 0 1 1-18 0 9 9 0 0 1 18 0Zm-9 3.75h.008v.008H12v-.008Z" />
            </svg>
          </div>
          <h1 className="text-xl font-bold text-white mb-2">Invalid or expired link</h1>
          <p className="text-slate-400 text-sm">
            This link is no longer valid. Please request a new one.
          </p>
        </div>
      </div>
    );
  }
 
  if (status === "success") {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <div className="w-full max-w-md bg-slate-800 rounded-lg p-8 border border-slate-700 text-center">
          <div className="w-14 h-14 bg-green-900/50 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-7 h-7 text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h1 className="text-xl font-bold text-white mb-2">Password set successfully</h1>
          <p className="text-slate-400 text-sm mb-6">You can now sign in with your new password.</p>
          <button
            onClick={() => router.push("/auth/login")}
            className="px-6 py-2.5 bg-blue-600 text-white rounded font-medium hover:bg-blue-700 transition"
          >
            Go to Sign In
          </button>
        </div>
      </div>
    );
  }
 
  const isInvite = flowType === "invite";
 
  return (
    <div className="min-h-[60vh] flex items-center justify-center">
      <div className="w-full max-w-md bg-slate-800 rounded-lg p-8 border border-slate-700">
        <h1 className="text-2xl font-bold text-white mb-2">
          {isInvite ? "Set your password" : "Reset your password"}
        </h1>
        <p className="text-slate-400 text-sm mb-6">
          {isInvite
            ? "Welcome! Choose a strong password to secure your account."
            : "Enter a new password for your account."}
        </p>
 
        <div className="space-y-5">
          {/* Password field */}
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-1.5">
              New password
            </label>
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={status === "loading"}
                placeholder="Minimum 8 characters"
                className="w-full px-4 py-2.5 pr-11 rounded bg-slate-900 text-white placeholder-slate-500 border border-slate-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50 transition"
              />
              <button
                type="button"
                onClick={() => setShowPassword((v) => !v)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-200 transition"
                tabIndex={-1}
              >
                {showPassword ? (
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M3.98 8.223A10.477 10.477 0 0 0 1.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.451 10.451 0 0 1 12 4.5c4.756 0 8.773 3.162 10.065 7.498a10.522 10.522 0 0 1-4.293 5.774M6.228 6.228 3 3m3.228 3.228 3.65 3.65m7.894 7.894L21 21m-3.228-3.228-3.65-3.65m0 0a3 3 0 1 0-4.243-4.243m4.242 4.242L9.88 9.88" />
                  </svg>
                ) : (
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M2.036 12.322a1.012 1.012 0 0 1 0-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178Z" />
                    <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" />
                  </svg>
                )}
              </button>
            </div>
 
            {/* Strength meter */}
            {password.length > 0 && (
              <div className="mt-2">
                <div className="flex gap-1 mb-1">
                  {[1, 2, 3, 4].map((i) => (
                    <div
                      key={i}
                      className={`h-1 flex-1 rounded-full transition-all duration-300 ${
                        strength.score >= i ? strength.color : "bg-slate-700"
                      }`}
                    />
                  ))}
                </div>
                <div className="flex justify-between items-center">
                  <p className="text-xs text-slate-500">
                    {password.length < 8 ? "At least 8 characters required" : ""}
                  </p>
                  {strength.label && (
                    <p className={`text-xs font-medium ${
                      strength.score === 1 ? "text-red-400" :
                      strength.score === 2 ? "text-orange-400" :
                      strength.score === 3 ? "text-yellow-400" :
                      "text-green-400"
                    }`}>
                      {strength.label}
                    </p>
                  )}
                </div>
              </div>
            )}
          </div>
 
          {/* Confirm password field */}
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-1.5">
              Confirm password
            </label>
            <div className="relative">
              <input
                type={showConfirm ? "text" : "password"}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                disabled={status === "loading"}
                placeholder="Repeat your password"
                className={`w-full px-4 py-2.5 pr-11 rounded bg-slate-900 text-white placeholder-slate-500 border focus:outline-none focus:ring-2 focus:border-transparent disabled:opacity-50 transition ${
                  passwordsMismatch
                    ? "border-red-500 focus:ring-red-500"
                    : passwordsMatch
                    ? "border-green-500 focus:ring-green-500"
                    : "border-slate-600 focus:ring-blue-500"
                }`}
              />
              <button
                type="button"
                onClick={() => setShowConfirm((v) => !v)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-200 transition"
                tabIndex={-1}
              >
                {showConfirm ? (
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M3.98 8.223A10.477 10.477 0 0 0 1.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.451 10.451 0 0 1 12 4.5c4.756 0 8.773 3.162 10.065 7.498a10.522 10.522 0 0 1-4.293 5.774M6.228 6.228 3 3m3.228 3.228 3.65 3.65m7.894 7.894L21 21m-3.228-3.228-3.65-3.65m0 0a3 3 0 1 0-4.243-4.243m4.242 4.242L9.88 9.88" />
                  </svg>
                ) : (
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M2.036 12.322a1.012 1.012 0 0 1 0-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178Z" />
                    <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" />
                  </svg>
                )}
              </button>
            </div>
            {passwordsMismatch && (
              <p className="mt-1.5 text-xs text-red-400">Passwords do not match.</p>
            )}
            {passwordsMatch && (
              <p className="mt-1.5 text-xs text-green-400">Passwords match.</p>
            )}
          </div>
 
          {/* Error */}
          {status === "error" && errorMessage && (
            <div className="p-3 bg-red-900/60 border border-red-700 rounded text-red-200 text-sm">
              {errorMessage}
            </div>
          )}
 
          {/* Submit */}
          <button
            onClick={handleSubmit}
            disabled={!isValid || status === "loading"}
            className="w-full px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded font-semibold disabled:opacity-50 hover:from-blue-600 hover:to-blue-700 transition flex items-center justify-center gap-2"
          >
            {status === "loading" ? (
              <>
                <span className="inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                Setting password...
              </>
            ) : isInvite ? (
              "Set Password"
            ) : (
              "Reset Password"
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
