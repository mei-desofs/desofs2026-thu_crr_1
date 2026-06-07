"use client";

import { useEffect, useState } from "react";
import { Toast } from "./useToast";

interface ToastContainerProps {
  toasts: Toast[];
  onRemove: (id: string) => void;
}

export function ToastContainer({ toasts, onRemove }: ToastContainerProps) {
  return (
    <div className="fixed top-8 left-1/2 transform -translate-x-1/2 z-50 space-y-2">
      {toasts.map((toast) => (
        <ToastItem
          key={toast.id}
          toast={toast}
          onRemove={() => onRemove(toast.id)}
        />
      ))}
    </div>
  );
}

interface ToastItemProps {
  toast: Toast;
  onRemove: () => void;
}

function ToastItem({ toast, onRemove }: ToastItemProps) {
  const [isClosing, setIsClosing] = useState(false);

  useEffect(() => {
    if (toast.duration !== 0) {
      const timer = setTimeout(() => {
        setIsClosing(true);
      }, (toast.duration || 3000) - 300);

      return () => clearTimeout(timer);
    }
  }, [toast.duration]);

  const bgColor = {
    success: "bg-green-600 border-green-500",
    error: "bg-red-600 border-red-500",
    info: "bg-blue-600 border-blue-500",
  }[toast.type];

  const icon = {
    success: "✓",
    error: "✕",
    info: "ℹ",
  }[toast.type];

  return (
    <div
      className={`
        flex items-center gap-3 px-6 py-4 rounded border
        ${bgColor}
        text-white text-sm
        animate-in fade-in
        ${isClosing ? "animate-out fade-out" : ""}
        transition-all duration-300
      `}
      onAnimationEnd={() => {
        if (isClosing) onRemove();
      }}
    >
      <span className="text-lg font-bold">{icon}</span>
      <span>{toast.message}</span>
      <button
        onClick={() => {
          setIsClosing(true);
        }}
        className="ml-auto text-white hover:opacity-70 transition"
      >
        ✕
      </button>
    </div>
  );
}