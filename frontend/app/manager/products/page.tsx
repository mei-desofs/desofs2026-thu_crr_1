"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { apiGet, apiDelete } from "@/lib/api";
import { useToast } from "@/app/components/useToast";

interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  categoryId: string;
  categoryName?: string;
  imageUrl?: string;
}

interface ProductsResponse {
  content: Product[];
  totalElements: number;
  totalPages: number;
}

type PageStatus = "loading" | "idle" | "error";

export default function ProductsManagementPage() {
  const router = useRouter();
  const { success, error: showError } = useToast();

  const [products, setProducts] = useState<Product[]>([]);
  const [pageStatus, setPageStatus] = useState<PageStatus>("loading");
  const [searchTerm, setSearchTerm] = useState("");
  const [deletingId, setDeletingId] = useState<string | null>(null);

  useEffect(() => {
    const loadProducts = async () => {
      try {
        const data = await apiGet<ProductsResponse>("/products");
        setProducts(data.content || []);
        setPageStatus("idle");
      } catch (err: any) {
        const errorMsg =
          err?.response?.data?.message || "Failed to load products";
        showError(errorMsg);
        setPageStatus("error");
      }
    };
    loadProducts();
  }, []);

  const handleDelete = async (productId: string) => {
    if (
      !confirm(
        "Are you sure you want to delete this product? This action cannot be undone.",
      )
    ) {
      return;
    }

    try {
      setDeletingId(productId);
      await apiDelete(`/products/${productId}`);
      setProducts((prev) => prev.filter((p) => p.id !== productId));
      success("Product deleted successfully");
    } catch (err: any) {
      const errorMsg =
        err?.response?.data?.message || "Failed to delete product";
      showError(errorMsg);
    } finally {
      setDeletingId(null);
    }
  };

  const filteredProducts = products.filter(
    (p) =>
      p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.description.toLowerCase().includes(searchTerm.toLowerCase()),
  );

  return (
    <div className="py-8">
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <button
            onClick={() => router.push("/manager/dashboard")}
            className="flex items-center gap-2 text-slate-400 hover:text-white transition text-sm mb-4"
          >
            <svg
              className="w-4 h-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth={2}
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M15.75 19.5L8.25 12l7.5-7.5"
              />
            </svg>
            Back to Dashboard
          </button>
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-4xl font-bold text-white mb-1">
                Manage Products
              </h1>
              <p className="text-slate-400">
                View, edit, and manage products in your catalogue.
              </p>
            </div>
            <button
              onClick={() => router.push("/products/manage")}
              className="px-6 py-3 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-lg font-semibold hover:from-blue-600 hover:to-blue-700 transition flex items-center gap-2"
            >
              <svg
                className="w-5 h-5"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                strokeWidth={2}
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M12 4.5v15m7.5-7.5h-15"
                />
              </svg>
              New Product
            </button>
          </div>
        </div>

        {/* Search */}
        <div className="mb-6">
          <div className="relative">
            <svg
              className="w-5 h-5 absolute left-3 top-1/2 -translate-y-1/2 text-slate-500"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth={2}
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.5 5.5a7.5 7.5 0 0 0 10.5 10.5Z"
              />
            </svg>
            <input
              type="text"
              placeholder="Search products..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2.5 rounded-lg bg-slate-800 text-white placeholder-slate-500 border border-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
        </div>

        {/* Loading state */}
        {pageStatus === "loading" && (
          <div className="flex items-center justify-center py-12">
            <div className="text-center">
              <div className="inline-block w-8 h-8 border-4 border-slate-700 border-t-blue-500 rounded-full animate-spin mb-3"></div>
              <p className="text-slate-400">Loading products...</p>
            </div>
          </div>
        )}

        {/* Error state */}
        {pageStatus === "error" && (
          <div className="bg-red-900/60 border border-red-700 rounded-lg p-4 text-red-200">
            Failed to load products. Please try refreshing the page.
          </div>
        )}

        {/* Empty state */}
        {pageStatus === "idle" && filteredProducts.length === 0 && (
          <div className="text-center py-12">
            <svg
              className="w-16 h-16 text-slate-600 mx-auto mb-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth={1.5}
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="m21 7.5-9-5.25L3 7.5m18 0-9 5.25m9-5.25v9l-9 5.25M3 7.5l9 5.25M3 7.5v9l9 5.25m0-9v9"
              />
            </svg>
            <h3 className="text-lg font-medium text-white mb-1">
              No products found
            </h3>
            <p className="text-slate-400 mb-6">
              {searchTerm
                ? "Try adjusting your search"
                : "Create your first product to get started"}
            </p>
            {!searchTerm && (
              <button
                onClick={() => router.push("/products/manage")}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition"
              >
                Create Product
              </button>
            )}
          </div>
        )}

        {/* Products table */}
        {pageStatus === "idle" && filteredProducts.length > 0 && (
          <div className="bg-slate-800 border border-slate-700 rounded-lg overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-slate-900 border-b border-slate-700">
                  <tr>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-slate-300">
                      Product
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-slate-300">
                      Price
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-slate-300">
                      Stock
                    </th>
                    <th className="px-6 py-4 text-left text-sm font-semibold text-slate-300">
                      Category
                    </th>
                    <th className="px-6 py-4 text-right text-sm font-semibold text-slate-300">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-700">
                  {filteredProducts.map((product) => (
                    <tr
                      key={product.id}
                      className="hover:bg-slate-700/50 transition"
                    >
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          {product.imageUrl && (
                            <div className="w-10 h-10 rounded bg-slate-700 flex-shrink-0 overflow-hidden">
                              <img
                                src={product.imageUrl}
                                alt={product.name}
                                className="w-full h-full object-cover"
                              />
                            </div>
                          )}
                          <div className="min-w-0">
                            <p className="text-white font-medium truncate">
                              {product.name}
                            </p>
                            <p className="text-slate-400 text-sm truncate">
                              {product.description.substring(0, 40)}...
                            </p>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <p className="text-white font-medium">
                          €{product.price.toFixed(2)}
                        </p>
                      </td>
                      <td className="px-6 py-4">
                        <div
                          className={`inline-flex px-2.5 py-1 rounded-full text-sm font-medium ${
                            product.stockQuantity > 0
                              ? "bg-green-900/50 text-green-200"
                              : "bg-red-900/50 text-red-200"
                          }`}
                        >
                          {product.stockQuantity} unit
                          {product.stockQuantity !== 1 ? "s" : ""}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <p className="text-slate-300 text-sm">
                          {product.categoryName || "—"}
                        </p>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() =>
                              router.push(`/manager/products/${product.id}`)
                            }
                            className="p-2 text-slate-400 hover:text-white hover:bg-slate-700 rounded transition"
                            title="Edit product"
                          >
                            <svg
                              className="w-5 h-5"
                              fill="none"
                              viewBox="0 0 24 24"
                              stroke="currentColor"
                              strokeWidth={2}
                            >
                              <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                d="m16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 9.75a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0z"
                              />
                            </svg>
                          </button>
                          <button
                            onClick={() => handleDelete(product.id)}
                            disabled={deletingId === product.id}
                            className="p-2 text-slate-400 hover:text-red-400 hover:bg-red-900/30 rounded transition disabled:opacity-50"
                            title="Delete product"
                          >
                            <svg
                              className="w-5 h-5"
                              fill="none"
                              viewBox="0 0 24 24"
                              stroke="currentColor"
                              strokeWidth={2}
                            >
                              <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                              />
                            </svg>
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
