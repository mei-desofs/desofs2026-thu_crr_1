"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { apiGet, apiPost, apiDelete } from "@/lib/api";
import { useSafeTextContent } from "@/lib/hooks";

interface CartItem {
  productId: string;
  productName: string;
  productDescription: string;
  quantity: number;
  price: number;
}

export default function CartPage() {
  const [cartItems, setCartItems] = useState<CartItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadCart = async () => {
      try {
        setLoading(true);
        setError(null);
        const items = await apiGet<CartItem[]>("/cart/items");
        setCartItems(items || []);
      } catch (err) {
        console.error("Failed to load cart:", err);
        setError("Failed to load cart items. Please try again later.");
      } finally {
        setLoading(false);
      }
    };

    loadCart();
  }, []);

  const calculateTotal = (): number => {
    return cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
  };

  const removeItem = async (productId: string) => {
    try {
      setError(null);
      await apiDelete(`/cart/items/${productId}`);
      setCartItems((prev) => prev.filter((item) => item.productId !== productId));
    } catch (err) {
      console.error("Failed to remove item:", err);
      setError("Failed to remove item. Please try again.");
    }
  };

  const updateQuantity = async (productId: string, quantityDelta: number) => {
    try {
      setError(null);
      await apiPost(`/cart/items/${productId}`, {
        quantityDelta,
      });

      setCartItems((prev) =>
        prev.map((item) =>
          item.productId === productId
            ? { ...item, quantity: item.quantity + quantityDelta }
            : item
        )
      );
    } catch (err) {
      console.error("Failed to update quantity:", err);
      setError("Failed to update item quantity. Please try again.");
    }
  };

  return (
    <div className="py-8">
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <h1 className="text-4xl font-bold text-white mb-8">Shopping Cart</h1>

        {error && (
          <div className="mb-6 p-4 bg-red-900 border border-red-700 rounded text-red-100">
            {error}
          </div>
        )}

        {loading ? (
          <div className="text-center py-12">
            <div className="inline-block animate-spin">
              <div className="w-12 h-12 border-4 border-slate-700 border-t-blue-500 rounded-full"></div>
            </div>
            <p className="text-slate-300 mt-4">Loading cart...</p>
          </div>
        ) : cartItems.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-slate-400 text-lg mb-6">Your cart is empty</p>
            <Link
              href="/products"
              className="inline-block px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
            >
              Start Shopping
            </Link>
          </div>
        ) : (
          <div className="grid lg:grid-cols-3 gap-8">
            {/* Cart Items */}
            <div className="lg:col-span-2 space-y-4">
              {cartItems.map((item) => (
                <CartItemRow
                  key={item.productId}
                  item={item}
                  onRemove={removeItem}
                  onUpdateQuantity={updateQuantity}
                />
              ))}
            </div>

            {/* Cart Summary */}
            <div className="bg-slate-800 rounded-lg border border-slate-700 p-6 h-fit sticky top-6">
              <h2 className="text-2xl font-bold text-white mb-6">
                Order Summary
              </h2>

              <div className="flex justify-between items-center mb-6">
                <span className="text-xl font-bold text-white">Total:</span>
                <span className="text-3xl font-bold text-green-400">
                  €{calculateTotal().toFixed(2)}
                </span>
              </div>

              <Link
                href="/checkout"
                className="block w-full text-center px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded font-semibold hover:from-blue-600 hover:to-blue-700 transition mb-3"
              >
                Proceed to Checkout
              </Link>

              <Link
                href="/products"
                className="block w-full text-center px-6 py-3 bg-slate-700 text-white rounded hover:bg-slate-600 transition"
              >
                Continue Shopping
              </Link>

            </div>
          </div>
        )}
      </main>
    </div>
  );
}

interface CartItemRowProps {
  item: CartItem;
  onRemove: (productId: string) => Promise<void>;
  onUpdateQuantity: (productId: string, quantityDelta: number) => Promise<void>;
}

/**
 * V3.2.2: Cart item row with safe text rendering
 */
function CartItemRow({ item, onRemove, onUpdateQuantity }: CartItemRowProps) {
  const nameRef = useSafeTextContent(item.productName);
  const descriptionRef = useSafeTextContent(item.productDescription);
  const [updating, setUpdating] = useState(false);

  const handleUpdateQuantity = async (delta: number) => {
    if (updating) return;
    setUpdating(true);
    try {
      await onUpdateQuantity(item.productId, delta);
    } finally {
      setUpdating(false);
    }
  };

  const handleRemove = async () => {
    if (updating) return;
    setUpdating(true);
    try {
      await onRemove(item.productId);
    } finally {
      setUpdating(false);
    }
  };

  return (
    <div className="bg-slate-800 rounded-lg border border-slate-700 p-6 flex gap-6">
      <div className="flex-1">
        <div ref={nameRef} className="text-lg font-semibold text-white mb-2" />
        <div ref={descriptionRef} className="text-slate-400 text-sm mb-4" />

        <div className="space-y-3">
          <div className="flex justify-between text-slate-300 mb-4">
            <span>Price per unit:</span>
            <span className="font-semibold">€{item.price.toFixed(2)}</span>
          </div>

          <div className="flex items-center gap-4">
            <label className="text-slate-300">Quantity:</label>
            <div className="flex items-center gap-2">
              <button
                onClick={() => handleUpdateQuantity(-1)}
                disabled={updating || item.quantity <= 1}
                className="px-3 py-1 bg-slate-700 text-white rounded hover:bg-slate-600 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                −
              </button>
              <span className="w-12 text-center text-white font-semibold text-lg">
                {item.quantity}
              </span>
              <button
                onClick={() => handleUpdateQuantity(1)}
                disabled={updating}
                className="px-3 py-1 bg-slate-700 text-white rounded hover:bg-slate-600 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                +
              </button>
            </div>
          </div>

          <div className="text-right text-lg font-bold text-white">
            Subtotal: €{(item.price * item.quantity).toFixed(2)}
          </div>
        </div>
      </div>

      <button
        onClick={handleRemove}
        disabled={updating}
        className="px-6 py-2 bg-red-900 text-red-100 rounded hover:bg-red-800 transition h-fit disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {updating ? "..." : "Remove"}
      </button>
    </div>
  );
}