"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { apiGet, apiPost } from "@/lib/api";
import axios from "axios";

interface CartItem {
  productId: string;
  productName: string;
  productDescription: string;
  quantity: number;
  price: number;
  imageDataUrl?: string | null;
}

interface AddressForm {
  street: string;
  city: string;
  postalCode: string;
  country: string;
}

interface AddressErrors {
  street?: string;
  city?: string;
  postalCode?: string;
  country?: string;
}

function validateAddress(address: AddressForm): AddressErrors {
  const errors: AddressErrors = {};
  if (!address.street.trim()) errors.street = "Street is required.";
  if (!address.city.trim()) errors.city = "City is required.";
  if (!address.country.trim()) errors.country = "Country is required.";
  if (!address.postalCode.trim()) {
    errors.postalCode = "Postal code is required.";
  } else if (!/^\d{4}-\d{3}$/.test(address.postalCode)) {
    errors.postalCode = "Must follow format XXXX-XXX (e.g. 1000-001).";
  }
  return errors;
}

function getErrorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    return err.response?.data?.message ?? "Request could not be processed.";
  }
  return "Request could not be processed.";
}

type PageStatus = "loading" | "ready" | "ordering" | "success" | "error";

export default function CheckoutPage() {
  const router = useRouter();
  const [cartItems, setCartItems] = useState<CartItem[]>([]);
  const [pageStatus, setPageStatus] = useState<PageStatus>("loading");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [address, setAddress] = useState<AddressForm>({
    street: "",
    city: "",
    postalCode: "",
    country: "",
  });
  const [addressErrors, setAddressErrors] = useState<AddressErrors>({});

  useEffect(() => {
    const loadCart = async () => {
      try {
        const items = await apiGet<CartItem[]>("/cart/items");
        if (!items || items.length === 0) {
          router.replace("/cart");
          return;
        }
        setCartItems(items);
        setPageStatus("ready");
      } catch {
        setErrorMessage("Failed to load cart. Please try again.");
        setPageStatus("error");
      }
    };
    loadCart();
  }, [router]);

  const calculateTotal = () =>
    cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);

  const handleAddressChange = (field: keyof AddressForm, value: string) => {
    setAddress((prev) => ({ ...prev, [field]: value }));
    if (addressErrors[field]) {
      setAddressErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  const handlePlaceOrder = async () => {
    const errors = validateAddress(address);
    if (Object.keys(errors).length > 0) {
      setAddressErrors(errors);
      return;
    }

    try {
      setPageStatus("ordering");
      setErrorMessage(null);
      await apiPost("/orders", { address });
      setPageStatus("success");
    } catch (err) {
      setErrorMessage(getErrorMessage(err));
      setPageStatus("ready");
    }
  };

  // ── Loading ──────────────────────────────────────────────────────────────
  if (pageStatus === "loading") {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin">
            <div className="w-12 h-12 border-4 border-slate-700 border-t-blue-500 rounded-full" />
          </div>
          <p className="text-slate-300 mt-4">Loading your cart...</p>
        </div>
      </div>
    );
  }

  // ── Success ──────────────────────────────────────────────────────────────
  if (pageStatus === "success") {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <div className="w-full max-w-md bg-slate-800 rounded-lg p-8 border border-slate-700 text-center">
          <div className="w-16 h-16 bg-green-900/50 rounded-full flex items-center justify-center mx-auto mb-5">
            <svg className="w-8 h-8 text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h1 className="text-2xl font-bold text-white mb-2">Order placed!</h1>
          <p className="text-slate-400 text-sm mb-8">
            Your order has been confirmed. A confirmation email has been sent to you.
          </p>
          <button
            onClick={() => router.push("/products")}
            className="w-full px-6 py-3 bg-blue-600 text-white rounded font-semibold hover:bg-blue-700 transition"
          >
            Continue Shopping
          </button>
        </div>
      </div>
    );
  }

  // ── Error loading cart ───────────────────────────────────────────────────
  if (pageStatus === "error") {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <div className="w-full max-w-md bg-slate-800 rounded-lg p-8 border border-slate-700 text-center">
          <p className="text-red-400 mb-4">{errorMessage}</p>
          <button onClick={() => router.push("/cart")} className="px-6 py-2 bg-slate-700 text-white rounded hover:bg-slate-600 transition">
            Back to Cart
          </button>
        </div>
      </div>
    );
  }

  // ── Checkout form ────────────────────────────────────────────────────────
  return (
    <div className="py-8">
      <main className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <button
            onClick={() => router.push("/cart")}
            className="flex items-center gap-2 text-slate-400 hover:text-white transition text-sm mb-4"
          >
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
            </svg>
            Back to Cart
          </button>
          <h1 className="text-4xl font-bold text-white">Checkout</h1>
        </div>

        <div className="grid lg:grid-cols-5 gap-8">
          {/* Address form */}
          <div className="lg:col-span-3 bg-slate-800 rounded-lg border border-slate-700 p-6">
            <h2 className="text-xl font-bold text-white mb-5">Delivery Address</h2>

            <div className="space-y-4">
              <AddressField
                label="Street"
                value={address.street}
                placeholder="e.g. Rua das Flores, 12"
                error={addressErrors.street}
                disabled={pageStatus === "ordering"}
                onChange={(v) => handleAddressChange("street", v)}
              />
              <AddressField
                label="City"
                value={address.city}
                placeholder="e.g. Lisboa"
                error={addressErrors.city}
                disabled={pageStatus === "ordering"}
                onChange={(v) => handleAddressChange("city", v)}
              />
              <div className="grid grid-cols-2 gap-4">
                <AddressField
                  label="Postal Code"
                  value={address.postalCode}
                  placeholder="XXXX-XXX"
                  error={addressErrors.postalCode}
                  disabled={pageStatus === "ordering"}
                  onChange={(v) => handleAddressChange("postalCode", v)}
                />
                <AddressField
                  label="Country"
                  value={address.country}
                  placeholder="e.g. Portugal"
                  error={addressErrors.country}
                  disabled={pageStatus === "ordering"}
                  onChange={(v) => handleAddressChange("country", v)}
                />
              </div>
            </div>

            {errorMessage && (
              <div className="mt-5 p-3 bg-red-900/60 border border-red-700 rounded text-red-200 text-sm">
                {errorMessage}
              </div>
            )}
          </div>

          {/* Order summary */}
          <div className="lg:col-span-2 space-y-4">
            <div className="bg-slate-800 rounded-lg border border-slate-700 p-6">
              <h2 className="text-xl font-bold text-white mb-4">Order Summary</h2>

              <div className="space-y-3 mb-4 max-h-56 overflow-y-auto pr-1">
                {cartItems.map((item) => (
                  <div
                    key={item.productId}
                    className="flex items-center gap-3 text-sm"
                  >
                    <div className="w-10 h-10 rounded bg-slate-900 border border-slate-700 overflow-hidden shrink-0">
                      {item.imageDataUrl ? (
                        <img
                          src={item.imageDataUrl}
                          alt={item.productName}
                          className="w-full h-full object-fill"
                        />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-slate-500 text-[8px]">
                          No img
                        </div>
                      )}
                    </div>

                    <div className="flex-1 min-w-0">
                      <p className="text-slate-300 truncate">
                        {item.productName}
                      </p>
                      <p className="text-slate-500 text-xs">
                        ×{item.quantity}
                      </p>
                    </div>

                    <span className="text-white font-medium shrink-0">
                      €{(item.price * item.quantity).toFixed(2)}
                    </span>
                  </div>
                ))}
              </div>

              <div className="border-t border-slate-700 pt-4 flex justify-between items-center mb-6">
                <span className="text-lg font-bold text-white">Total</span>
                <span className="text-2xl font-bold text-green-400">
                  €{calculateTotal().toFixed(2)}
                </span>
              </div>

              <button
                onClick={handlePlaceOrder}
                disabled={pageStatus === "ordering"}
                className="w-full px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded font-semibold disabled:opacity-50 hover:from-blue-600 hover:to-blue-700 transition flex items-center justify-center gap-2"
              >
                {pageStatus === "ordering" ? (
                  <>
                    <span className="inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    Placing order...
                  </>
                ) : (
                  "Place Order"
                )}
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}


interface AddressFieldProps {
  label: string;
  value: string;
  placeholder: string;
  error?: string;
  disabled?: boolean;
  onChange: (value: string) => void;
}

function AddressField({ label, value, placeholder, error, disabled, onChange }: AddressFieldProps) {
  return (
    <div>
      <label className="block text-sm font-medium text-slate-300 mb-1.5">{label}</label>
      <input
        type="text"
        value={value}
        placeholder={placeholder}
        disabled={disabled}
        onChange={(e) => onChange(e.target.value)}
        className={`w-full px-3 py-2.5 rounded bg-slate-900 text-white placeholder-slate-500 border focus:outline-none focus:ring-2 focus:border-transparent transition text-sm disabled:opacity-50 ${
          error
            ? "border-red-500 focus:ring-red-500"
            : "border-slate-600 focus:ring-blue-500"
        }`}
      />
      {error && <p className="mt-1 text-xs text-red-400">{error}</p>}
    </div>
  );
}