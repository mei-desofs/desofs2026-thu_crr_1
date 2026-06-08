"use client";

import { ToastContainer } from "./ToastContainer";
import { useToast } from "./useToast";

export default function ToastWrapper() {
  const { toasts, removeToast } = useToast();
  return <ToastContainer toasts={toasts} onRemove={removeToast} />;
}