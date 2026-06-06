"use client";

import { useEffect, useState } from "react";
import { apiGet, apiPost } from "@/lib/api";
import { useRouter } from "next/navigation";
import { useSafeTextContent } from "@/lib/hooks";

interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
}

interface ProductResponse {
  content: Product[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [searchQuery, setSearchQuery] = useState("");
  const [inputValue, setInputValue] = useState("");
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    const loadProducts = async () => {
      try {
        setLoading(true);
        setError(null);

        const endpoint = searchQuery
          ? `/products/search?productName=${encodeURIComponent(searchQuery)}&page=${currentPage}`
          : `/products?page=${currentPage}`;

        const response = await apiGet<ProductResponse>(endpoint);
        setProducts(response.content);
        setTotalPages(response.totalPages);
      } catch (err) {
        setError("Failed to load products. Please try again later.");
        console.error("Error loading products:", err);
      } finally {
        setLoading(false);
      }
    };

    loadProducts();
  }, [currentPage, searchQuery]);

  const handleSearch = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setSearchQuery(inputValue);
    setCurrentPage(0);
  };

  return (
    <div className="py-8">
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-white mb-6">Products</h1>

          {/* Search Form */}
          <form onSubmit={handleSearch} className="flex gap-2 mb-6">
            <input
              type="text"
              placeholder="Search products..."
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              className="flex-1 px-4 py-2 rounded bg-slate-800 text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <button
              type="submit"
              className="px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
            >
              Search
            </button>
          </form>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-6 p-4 bg-red-900 border border-red-700 rounded text-red-100">
            {error}
          </div>
        )}

        {/* Loading State */}
        {loading && (
          <div className="text-center py-12">
            <div className="inline-block animate-spin">
              <div className="w-12 h-12 border-4 border-slate-700 border-t-blue-500 rounded-full"></div>
            </div>
            <p className="text-slate-300 mt-4">Loading products...</p>
          </div>
        )}

        {/* Products Grid */}
        {!loading && products.length > 0 && (
          <>
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
              {products.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex justify-center gap-2 mt-8">
                <button
                  onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                  disabled={currentPage === 0}
                  className="px-4 py-2 bg-slate-700 text-white rounded disabled:opacity-50 hover:bg-slate-600"
                >
                  Previous
                </button>
                <span className="px-4 py-2 text-slate-300">
                  Page {currentPage + 1} of {totalPages}
                </span>
                <button
                  onClick={() =>
                    setCurrentPage(Math.min(totalPages - 1, currentPage + 1))
                  }
                  disabled={currentPage === totalPages - 1}
                  className="px-4 py-2 bg-slate-700 text-white rounded disabled:opacity-50 hover:bg-slate-600"
                >
                  Next
                </button>
              </div>
            )}
          </>
        )}

        {/* No Products State */}
        {!loading && products.length === 0 && !error && (
          <div className="text-center py-12">
            <p className="text-slate-400 text-lg">No products found.</p>
          </div>
        )}
      </main>
    </div>
  );
}

interface ProductCardProps {
  product: Product;
}

/**
 * V3.2.2: Product card with safe text rendering
 * Uses textContent and safe rendering to prevent XSS
 */
function ProductCard({ product }: ProductCardProps) {
  const nameRef = useSafeTextContent(product.name);
  const descriptionRef = useSafeTextContent(product.description);
  const router = useRouter();
  const [adding, setAdding] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const handleAddToCart = async () => {
    if (product.stockQuantity === 0 || adding) return;

    setAdding(true);
    setMessage(null);

    try {
      await apiPost("/cart/items", { productId: product.id, quantity: 1 });
      setMessage("Added to cart");
    } catch (err: any) {
      console.error("Add to cart failed:", err);
      const status = err?.response?.status;
      if (status === 401 || status === 403) {
        // Not authenticated — redirect to login with message and next url
        const message = encodeURIComponent(
          "Please sign in to add items to your cart",
        );
        const next = encodeURIComponent(`/products`);
        router.push(`/auth/login?message=${message}&next=${next}`);
      } else {
        setMessage("Failed to add to cart. Try again.");
      }
    } finally {
      setAdding(false);
      window.setTimeout(() => setMessage(null), 3000);
    }
  };

  return (
    <div className="bg-slate-800 rounded-lg border border-slate-700 overflow-hidden hover:border-blue-500 transition">
      <div className="p-6">
        <div
          ref={nameRef}
          className="text-xl font-bold text-white mb-2 h-8 overflow-hidden"
        />
        <div
          ref={descriptionRef}
          className="text-slate-400 text-sm mb-4 h-12 overflow-hidden"
        />

        <div className="space-y-3">
          <div className="flex justify-between items-center">
            <span className="text-slate-400">Price:</span>
            <span className="text-2xl font-bold text-green-400">
              €{product.price.toFixed(2)}
            </span>
          </div>
          <div className="flex justify-between items-center">
            <span className="text-slate-400">Stock:</span>
            <span
              className={`font-semibold ${
                product.stockQuantity > 0 ? "text-green-400" : "text-red-400"
              }`}
            >
              {product.stockQuantity > 0
                ? `${product.stockQuantity} available`
                : "Out of stock"}
            </span>
          </div>

          <div>
            <button
              onClick={handleAddToCart}
              disabled={product.stockQuantity === 0 || adding}
              className="w-full mt-4 px-4 py-2 bg-blue-600 text-white rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-blue-700 transition flex items-center justify-center"
            >
              {adding ? (
                <>
                  <span className="inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin mr-2" />
                  Adding...
                </>
              ) : product.stockQuantity > 0 ? (
                "Add to Cart"
              ) : (
                "Out of Stock"
              )}
            </button>

            {message && (
              <div className="mt-2 text-sm text-slate-300">{message}</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
