"use client";

import { useEffect, useState } from "react";
import { apiGet } from "@/lib/api";
import Link from "next/link";
import { useSafeTextContent } from "@/lib/hooks";
import { useRouter } from "next/navigation";

interface CartItem {
  id: string;
  name: string;
  price: number;
  quantity: number;
}

export default function CartPage() {
  const [cartItems, setCartItems] = useState<CartItem[]>([]);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const loadCart = async () => {
      setLoading(true);

      try {
        const serverResp = await apiGet<any>('/cart');
        const productsMap = serverResp?.products;

        if (productsMap && typeof productsMap === 'object') {
            const items: CartItem[] = Object.entries(productsMap).map(
                ([id, cartProduct]: [string, any]) => ({
                    id,
                    name: cartProduct.productName,
                    quantity: cartProduct.quantity,
                    price: cartProduct.unitPrice,
                })
            );

            setCartItems(items);
            setLoading(false);
            return;
        }
      } catch (err: any) {
        // If endpoint not available or returns error, fall back to localStorage
        console.debug(
          "Server cart unavailable, falling back to localStorage:",
          err?.message || err,
        );
      }

      // Fallback: load cart from localStorage (demo)
      try {
        const savedCart = localStorage.getItem("cart");
        if (savedCart) {
          const items = JSON.parse(savedCart);
          setCartItems(items);
        }
      } catch (e) {
        console.error("Failed to load cart:", e);
      } finally {
        setLoading(false);
      }
    };

    loadCart();
  }, []);

  const calculateTotal = (): number => {
    return cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
  };

  const removeItem = (id: string) => {
    const updated = cartItems.filter((item) => item.id !== id);
    setCartItems(updated);
    localStorage.setItem("cart", JSON.stringify(updated));
  };

  const updateQuantity = (id: string, quantity: number) => {
    if (quantity <= 0) {
      removeItem(id);
      return;
    }
    const updated = cartItems.map((item) =>
      item.id === id ? { ...item, quantity } : item,
    );
    setCartItems(updated);
    localStorage.setItem("cart", JSON.stringify(updated));
  };

  return (
    <div className="py-8">
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <h1 className="text-4xl font-bold text-white mb-8">Shopping Cart</h1>

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
                  key={item.id}
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

              <div className="space-y-4 mb-6 border-b border-slate-700 pb-6">
                <div className="flex justify-between text-slate-300">
                  <span>Subtotal:</span>
                  <span>€{calculateTotal().toFixed(2)}</span>
                </div>
              </div>

              <div className="flex justify-between items-center mb-6">
                <span className="text-xl font-bold text-white">Total:</span>
                <span className="text-3xl font-bold text-green-400">
                  €{calculateTotal().toFixed(2)}
                </span>
              </div>

              <button
                disabled={cartItems.length === 0}
                className="w-full px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded font-semibold disabled:opacity-50 hover:from-blue-600 hover:to-blue-700 transition mb-3"
              >
                Proceed to Checkout
              </button>

              <button
                onClick={() => router.push('/products')}
                className="w-full px-6 py-3 bg-slate-700 text-white rounded hover:bg-slate-600 transition"
              >
                Continue Shopping
              </button>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

interface CartItemRowProps {
  item: CartItem;
  onRemove: (id: string) => void;
  onUpdateQuantity: (id: string, quantity: number) => void;
}

/**
 * V3.2.2: Cart item row with safe text rendering
 */
function CartItemRow({ item, onRemove, onUpdateQuantity }: CartItemRowProps) {
  const nameRef = useSafeTextContent(item.name);

  return (
    <div className="bg-slate-800 rounded-lg border border-slate-700 p-6 flex gap-6">
      <div className="flex-1">
        <div ref={nameRef} className="text-lg font-semibold text-white mb-4" />

        <div className="space-y-3">
          <div className="flex justify-between text-slate-300 mb-4">
            <span>Price per unit:</span>
            <span className="font-semibold">€{item.price.toFixed(2)}</span>
          </div>

          <div className="flex items-center gap-4">
            <label className="text-slate-300">Quantity:</label>
            <div className="flex items-center gap-2">
              <button
                onClick={() => onUpdateQuantity(item.id, item.quantity - 1)}
                className="px-3 py-1 bg-slate-700 text-white rounded hover:bg-slate-600"
              >
                −
              </button>
              <input
                type="number"
                min="1"
                value={item.quantity}
                onChange={(e) =>
                  onUpdateQuantity(item.id, parseInt(e.target.value) || 1)
                }
                className="w-16 px-2 py-1 bg-slate-700 text-white rounded text-center focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <button
                onClick={() => onUpdateQuantity(item.id, item.quantity + 1)}
                className="px-3 py-1 bg-slate-700 text-white rounded hover:bg-slate-600"
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
        onClick={() => onRemove(item.id)}
        className="px-6 py-2 bg-red-900 text-red-100 rounded hover:bg-red-800 transition h-fit"
      >
        Remove
      </button>
    </div>
  );
}
