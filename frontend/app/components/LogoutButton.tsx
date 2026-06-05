'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { apiPost } from '@/lib/api';

export default function LogoutButton() {
  const [showConfirm, setShowConfirm] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  const handleLogoutConfirm = async () => {
    try {
      setLoggingOut(true);
      setError(null);

      await apiPost('/auth/logout');

      setShowConfirm(false);
      setLoggingOut(false);
      router.push('/auth/login');
      router.refresh();
    } catch (err: unknown) {
      console.error('Logout error:', err);
      setError('Failed to logout. Please try again.');
      setLoggingOut(false);
    }
  };

  const handleClose = () => {
    setShowConfirm(false);
    setError(null);
    setLoggingOut(false);
  };

  return (
    <>
      <button
        onClick={() => setShowConfirm(true)}
        disabled={loggingOut}
        className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed transition"
      >
        {loggingOut ? 'Logging out...' : 'LogOut'}
      </button>

      {showConfirm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-slate-800 rounded-lg p-6 max-w-sm w-full mx-4 shadow-lg border border-slate-700">
            <h2 className="text-xl font-bold text-white mb-4">
              Are you sure you want to log out?
            </h2>

            {error && (
              <div className="mb-4 p-3 bg-red-900 border border-red-700 rounded text-red-100 text-sm">
                {error}
              </div>
            )}

            <div className="flex gap-4 justify-end">
              <button
                onClick={handleClose}
                disabled={loggingOut}
                className="px-4 py-2 bg-slate-600 text-white rounded hover:bg-slate-500 transition disabled:opacity-50"
              >
                No
              </button>
              <button
                onClick={handleLogoutConfirm}
                disabled={loggingOut}
                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition disabled:opacity-50"
              >
                {loggingOut ? 'Logging out...' : 'Yes'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}