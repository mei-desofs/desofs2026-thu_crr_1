"use client";

import { useEffect, useState } from "react";
import { apiGet } from "@/lib/api";
import { useSafeTextContent } from "@/lib/hooks";
import apiClient from "@/lib/api";
import { isAxiosError } from "axios";

interface Address {
  postalCode: string;
  city: string;
  country: string;
  street: string;
}

interface OrderItem {
  productId: string;
  productName: string;
  quantity: number;
  price: number;
}

interface Order {
  orderId: string;
  status: string;
  totalPrice: number;
  address: Address;
  items: OrderItem[];
}

export default function CarrierPickupPage() {
  const [orders, setOrders]     = useState<Order[]>([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const loadOrders = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await apiGet<Order[]>("/orders/pending");
      setOrders(data ?? []);
    } catch {
      setError("Failed to load orders. Please try again later.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadOrders(); }, []);

  const handlePickupSuccess = (orderId: string) => {

    setOrders((prev) => prev.filter((o) => o.orderId !== orderId));
    setSuccessMessage("✓ Order picked up successfully.");

    setTimeout(() => {
      setSuccessMessage(null);
      setOrders((prev) => prev.filter((o) => o.orderId !== orderId));
    }, 2000);
  };

  return (
    <div className="py-8">
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <h1 className="text-4xl font-bold text-white mb-8">Orders to Pick Up</h1>

        {error && (
          <div className="mb-6 p-4 bg-red-900 border border-red-700 rounded text-red-100">
            {error}
          </div>
        )}

        {successMessage  && (
          <div className="mb-6 p-4 bg-green-900 border border-green-700 rounded text-green-100">
             {successMessage}
          </div>
        )}

        {loading ? (
          <div className="text-center py-12">
            <div className="w-12 h-12 border-4 border-slate-700 border-t-blue-500 rounded-full animate-spin mx-auto" />
            <p className="text-slate-300 mt-4">Loading orders…</p>
          </div>
        ) : orders.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-slate-400 text-lg">No pending orders to pick up.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {orders.map((order, index) => (
              <PickupOrderCard
                key={order.orderId}
                order={order}
                orderNumber={orders.length - index}
                onPickupSuccess={handlePickupSuccess}
              />
            ))}
          </div>
        )}
      </main>
    </div>
  );
}

function PickupOrderCard({
  order,
  orderNumber,
  onPickupSuccess,
}: {
  order: Order;
  orderNumber: number;
  onPickupSuccess: (orderId: string) => void;
}) {
  const [open, setOpen]               = useState(false);
  const [picking, setPicking]         = useState(false);
  const [pickupError, setPickupError] = useState<string | null>(null);

  const handlePickup = async () => {
    setPicking(true);
    setPickupError(null);
    try {
      await apiClient.patch(`/orders/${order.orderId}/pickup`);
      onPickupSuccess(order.orderId);
    } catch (err) {
      if (isAxiosError(err) && err.response?.status === 409) {
        setPickupError("This order has already been picked up.");
      } else {
        // Generic message — no internal details exposed (V16.5.1)
        setPickupError("Failed to confirm pickup. Please try again.");
      }
    } finally {
      setPicking(false);
    }
  };

  return (
    <div className="bg-slate-800 rounded-lg border border-slate-700 overflow-hidden">
      <button
        onClick={() => setOpen((v) => !v)}
        className="w-full flex items-center justify-between px-6 py-4 hover:bg-slate-750 transition text-left"
      >
        <div className="flex items-center gap-4">
          {/* Sequential number only — no UUID exposed (V14.2.6) */}
          <span className="text-white font-semibold">Order #{orderNumber}</span>
          <span className="text-xs font-semibold px-2 py-1 rounded bg-yellow-800 text-yellow-200">
            Pending
          </span>
        </div>
        <div className="flex items-center gap-6">
          <span className="text-green-400 font-bold">
            €{Number(order.totalPrice).toFixed(2)}
          </span>
          <span className="text-slate-400 text-sm">{open ? "▲" : "▼"}</span>
        </div>
      </button>

      {open && (
        <div className="px-6 pb-6 border-t border-slate-700 pt-4 space-y-4">
          <div>
            <p className="text-slate-400 text-xs uppercase tracking-wide mb-1">
              Delivery Address
            </p>
            <p className="text-white text-sm">
              {order.address.street}, {order.address.city},{" "}
              {order.address.postalCode}, {order.address.country}
            </p>
          </div>

          <div>
            <p className="text-slate-400 text-xs uppercase tracking-wide mb-2">Items</p>
            <div className="space-y-2">
              {order.items.map((item) => (
                <OrderItemRow key={item.productId} item={item} />
              ))}
            </div>
          </div>

          <div className="pt-2">
            {pickupError && (
              <p className="text-red-400 text-sm mb-2">{pickupError}</p>
            )}
            <button
              onClick={handlePickup}
              disabled={picking}
              className="px-5 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
            >
              {picking ? "Confirming…" : "Mark as Picked Up"}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

function OrderItemRow({ item }: { item: OrderItem }) {
  const nameRef = useSafeTextContent(item.productName); // V3.2.2

  return (
    <div className="flex justify-between items-center text-sm">
      <div className="flex items-center gap-2">
        <span className="text-slate-400">×{item.quantity}</span>
        <span ref={nameRef} className="text-white" />
      </div>
      <span className="text-slate-300">
        €{(Number(item.price) * item.quantity).toFixed(2)}
      </span>
    </div>
  );
}