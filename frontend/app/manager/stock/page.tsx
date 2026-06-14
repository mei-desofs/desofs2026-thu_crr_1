"use client";

import { useState, useEffect } from "react";
import { apiGet, apiPut } from "@/lib/api";
import { useToast } from "@/app/components/useToast";

interface Product {
  id: string;
  name: string;
  stockQuantity: number;
  price: number;
}

interface UpdateStockRequest {
  quantity: number;
}

interface ProductResponse {
  id: string;
  name: string;
  stockQuantity: number;
  price: number;
}

export default function StockManagement() {
  const [products, setProducts] = useState<Product[]>([]);
  const [selectedProductId, setSelectedProductId] = useState<string>("");
  const [newStock, setNewStock] = useState<string>("");
  const [loading, setLoading] = useState(false);
  const { success, error: showError } = useToast();
  useEffect(() => {

    const fetchProducts = async () => {
      try {
        setLoading(true);

        const res = await apiGet<any>("/products");

        const productsArray = res?.content ?? [];

        setProducts(productsArray);
      } catch (err: any) {
        const errorMsg =
          err?.response?.data?.error || "Falha ao carregar produtos";

        showError(errorMsg);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();

  }, []);

  const selectedProduct = products.find((p) => p.id === selectedProductId);

  const handleUpdateStock = async () => {
    if (!selectedProductId || newStock === "") {
      showError("Seleciona um produto e introduz a quantidade");
      return;
    }

    const quantity = parseInt(newStock, 10);
    if (isNaN(quantity) || quantity < 0) {
      showError("O stock deve ser um número >= 0");
      return;
    }

    try {
      setLoading(true);

      const request: UpdateStockRequest = { quantity };
      const response = await apiPut<ProductResponse>(
        `/products/${selectedProductId}/stock`,
        request,
      );

      success(`Stock atualizado! Novo stock: ${response.stockQuantity}`);
      setNewStock("");

      // Atualiza a lista local
      setProducts((prev) =>
        prev.map((p) =>
          p.id === selectedProductId ? { ...p, stockQuantity: response.stockQuantity } : p,
        ),
      );
    } catch (err: any) {
      const errorMsg =
        err?.response?.data?.error || "Falha ao carregar produtos";

      showError(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="py-8">
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <p className="text-blue-400 text-sm uppercase tracking-widest mb-1 font-medium">
            Manager Portal
          </p>
          <h1 className="text-3xl font-bold text-white mb-2">
            Stock Management
          </h1>
          <p className="text-slate-400">Update product stock levels</p>
        </div>

        {/* Card */}
        <div className="bg-slate-800 border border-slate-700 rounded-xl p-8">
          <div className="space-y-6">
            {/* Product Selection */}
            <div>
              <label className="block text-sm font-medium text-white mb-2">
                Select Product
              </label>
              <select
                value={selectedProductId}
                onChange={(e) => {
                  setSelectedProductId(e.target.value);
                  setNewStock("");
                                }}
                disabled={loading}
                className="w-full px-4 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
              >
                <option value="">Choose a product...</option>
                {products.map((product) => (
                  <option key={product.id} value={product.id}>
                    {product.name} 
                  </option>
                ))}
              </select>
            </div>

            {/* Current Stock Display */}
            {selectedProduct && (
              <div className="p-4 bg-slate-700/50 border border-slate-600 rounded-lg">
                <div className="grid grid-cols-3 gap-4">
                  <div>
                    <p className="text-slate-400 text-sm">Product Name</p>
                    <p className="text-white font-semibold mt-1">
                      {selectedProduct.name}
                    </p>
                  </div>
                  <div>
                    <p className="text-slate-400 text-sm">Current Stock</p>
                    <p className="text-white font-semibold mt-1">
                      {selectedProduct.stockQuantity} units
                    </p>
                  </div>
                  <div>
                    <p className="text-slate-400 text-sm">Price</p>
                    <p className="text-white font-semibold mt-1">
                      €{selectedProduct.price.toFixed(2)}
                    </p>
                  </div>
                </div>
              </div>
            )}

            {/* Stock Input */}
            {selectedProduct && (
              <div>
                <label className="block text-sm font-medium text-white mb-2">
                  New Stock Quantity
                </label>
                <div className="flex gap-3">
                  <input
                    type="number"
                    min="0"
                    value={newStock}
                    onChange={(e) => {
                      setNewStock(e.target.value);
                    }}
                    disabled={loading}
                    placeholder="Enter new stock quantity"
                    className="flex-1 px-4 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white placeholder-slate-400 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
                  />
                  <button
                    onClick={handleUpdateStock}
                    disabled={loading || !selectedProduct || newStock === ""}
                    className="px-6 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-slate-600 disabled:cursor-not-allowed text-white font-medium rounded-lg transition-colors duration-200"
                  >
                    {loading ? "Updating..." : "Change"}
                  </button>
                </div>
                <p className="text-slate-400 text-xs mt-2">
                  Stock must be a number greater than or equal to 0
                </p>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
