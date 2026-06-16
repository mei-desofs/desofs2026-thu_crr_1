"use client";
 
import { useEffect, useState } from "react";
import { apiGet } from "@/lib/api";
import { useSafeTextContent } from "@/lib/hooks";
 
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
  imageDataUrl?: string | null;
}
 
interface Order {
  orderId: string;
  status: string;
  totalPrice: number;
  address: Address;
  items: OrderItem[];
}
 
const STATUS_LABELS: Record<string, { label: string; className: string }> = {
  PENDING:   { label: "Pending",   className: "bg-yellow-800 text-yellow-200" },
  PICKED_UP: { label: "Picked Up", className: "bg-blue-800 text-blue-200"   },
  SHIPPED:   { label: "Shipped",   className: "bg-indigo-800 text-indigo-200" },
  DELIVERED: { label: "Delivered", className: "bg-green-800 text-green-200"  },
  CANCELLED: { label: "Cancelled", className: "bg-red-900 text-red-200"     },
};
 
export default function CustomerOrdersPage() {
  const [orders, setOrders]   = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState<string | null>(null);
 
  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await apiGet<Order[]>("/orders");
        setOrders(data ?? []);
      } catch {
        setError("Failed to load orders. Please try again later.");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);
 
  return (
    <div className="py-8">
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <h1 className="text-4xl font-bold text-white mb-8">My Orders</h1>
 
        {error && (
          <div className="mb-6 p-4 bg-red-900 border border-red-700 rounded text-red-100">
            {error}
          </div>
        )}
 
        {loading ? (
          <div className="text-center py-12">
            <div className="w-12 h-12 border-4 border-slate-700 border-t-blue-500 rounded-full animate-spin mx-auto" />
            <p className="text-slate-300 mt-4">Loading orders…</p>
          </div>
        ) : orders.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-slate-400 text-lg mb-4">You have no orders yet.</p>
            <a
              href="/app/products"
              className="inline-block px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
            >
              Start Shopping
            </a>
          </div>
        ) : (
          <div className="space-y-4">
            {orders.map((order, index) => (
              <OrderCard key={order.orderId} order={order} orderNumber={index + 1} />
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
 
function OrderCard({ order, orderNumber }: { order: Order; orderNumber: number }) {
  const [open, setOpen] = useState(false);
  const status = STATUS_LABELS[order.status] ?? { label: order.status, className: "bg-slate-700 text-slate-200" };
 
  return (
    <div className="bg-slate-800 rounded-lg border border-slate-700 overflow-hidden">
      {/* Header row */}
      <button
        onClick={() => setOpen((v) => !v)}
        className="w-full flex items-center justify-between px-6 py-4 hover:bg-slate-750 transition text-left"
      >
        <div className="flex items-center gap-4">
          <span className="text-white font-semibold">Order #{orderNumber}</span>
          <span className={`text-xs font-semibold px-2 py-1 rounded ${status.className}`}>
            {status.label}
          </span>
        </div>
        <div className="flex items-center gap-6">
          <span className="text-green-400 font-bold">
            €{Number(order.totalPrice).toFixed(2)}
          </span>
          <span className="text-slate-400 text-sm">{open ? "▲" : "▼"}</span>
        </div>
      </button>
 
      {/* Expandable detail */}
      {open && (
        <div className="px-6 pb-6 border-t border-slate-700 pt-4 space-y-4">
          {/* Address */}
          <div>
            <p className="text-slate-400 text-xs uppercase tracking-wide mb-1">
              Delivery Address
            </p>
            <p className="text-white text-sm">
              {order.address.street}, {order.address.city},{" "}
              {order.address.postalCode}, {order.address.country}
            </p>
          </div>
 
          {/* Items */}
          <div>
            <p className="text-slate-400 text-xs uppercase tracking-wide mb-2">
              Items
            </p>
            <div className="space-y-2">
              {order.items.map((item) => (
                <OrderItemRow key={item.productId} item={item} />
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
 
function OrderItemRow({ item }: { item: OrderItem }) {
  const nameRef = useSafeTextContent(item.productName);
 
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