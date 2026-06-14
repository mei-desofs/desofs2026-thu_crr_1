"use client";

import { useState, useEffect, useCallback } from "react";
import { apiGet } from "@/lib/api";
import { useToast } from "@/app/components/useToast";

interface OrderItem {
  productId: string;
  productName: string;
  quantity: number;
  price: number;
}

interface ManagerOrder {
  id: string;
  customerEmail: string;
  status: string;
  totalAmount: number;
  items: OrderItem[];
  createdAt: string;
}

interface OrdersPage {
  content: ManagerOrder[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

type OrderStatus =
  | "PENDING"
  | "PROCESSING"
  | "SHIPPED"
  | "DELIVERED"
  | "CANCELLED"
  | "";

export default function OrdersManager() {
  const [orders, setOrders] = useState<ManagerOrder[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    currentPage: 0,
    pageSize: 10,
    totalPages: 0,
    totalElements: 0,
  });
  const { error: showError } = useToast();

  // Filters
  const [filters, setFilters] = useState({
    status: "" as OrderStatus,
    customerEmail: "",
    startDate: "",
    endDate: "",
  });

  const fetchOrders = useCallback(
    async (page: number = 0) => {
      try {
        setLoading(true);

        const params = new URLSearchParams();
        params.append("page", page.toString());
        params.append("size", "10");
        params.append("sort", "createdAt,desc");

        if (filters.status) {
          params.append("status", filters.status);
        }
        if (filters.customerEmail) {
          params.append("customerEmail", filters.customerEmail);
        }
        if (filters.startDate) {
          params.append("startDate", new Date(filters.startDate).toISOString());
        }
        if (filters.endDate) {
          const endDate = new Date(filters.endDate);
          endDate.setHours(23, 59, 59, 999);
          params.append("endDate", endDate.toISOString());
        }

        const data = await apiGet<OrdersPage>(
          `/orders/manager?${params.toString()}`,
        );

        setOrders(data.content);
        setPagination({
          currentPage: data.currentPage,
          pageSize: data.pageSize,
          totalPages: data.totalPages,
          totalElements: data.totalElements,
        });
      } catch (err: any) {
        const errorMsg =
          err?.response?.data?.error || "Falha ao carregar produtos";

        showError(errorMsg);
      } finally {
        setLoading(false);
      }
    },
    [filters, showError],
  );

  useEffect(() => {
    fetchOrders(0);
  }, []);

  const handleFilterChange = (key: string, value: string) => {
    setFilters((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  const handleApplyFilters = () => {
    fetchOrders(0);
  };

  const handleClearFilters = () => {
    setFilters({
      status: "",
      customerEmail: "",
      startDate: "",
      endDate: "",
    });
  };

  const handlePageChange = (page: number) => {
    if (page >= 0 && page < pagination.totalPages) {
      fetchOrders(page);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "PENDING":
        return "bg-yellow-900/20 text-yellow-200 border-yellow-700";
      case "PROCESSING":
        return "bg-blue-900/20 text-blue-200 border-blue-700";
      case "SHIPPED":
        return "bg-purple-900/20 text-purple-200 border-purple-700";
      case "DELIVERED":
        return "bg-green-900/20 text-green-200 border-green-700";
      case "CANCELLED":
        return "bg-red-900/20 text-red-200 border-red-700";
      default:
        return "bg-slate-900/20 text-slate-200 border-slate-700";
    }
  };

  return (
    <div className="py-8">
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <p className="text-blue-400 text-sm uppercase tracking-widest mb-1 font-medium">
            Manager Portal
          </p>
          <h1 className="text-3xl font-bold text-white mb-2">
            Orders Management
          </h1>
          <p className="text-slate-400">View and manage customer orders</p>
        </div>

        {/* Filters Card */}
        <div className="bg-slate-800 border border-slate-700 rounded-xl p-6 mb-8">
          <h2 className="text-white font-semibold mb-4">Filters</h2>

          <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
            {/* Status Filter */}
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-1">
                Estado
              </label>
              <select
                value={filters.status}
                onChange={(e) => handleFilterChange("status", e.target.value)}
                className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white text-sm focus:outline-none focus:border-blue-500"
              >
                <option value="">Todos os Estados</option>
                <option value="PENDING">Pendente</option>
                <option value="PROCESSING">A Processar</option>
                <option value="SHIPPED">Enviado</option>
                <option value="DELIVERED">Entregue</option>
                <option value="CANCELLED">Cancelado</option>
              </select>
            </div>

            {/* Email Filter */}
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-1">
                Email do Cliente
              </label>
              <input
                type="email"
                value={filters.customerEmail}
                onChange={(e) =>
                  handleFilterChange("customerEmail", e.target.value)
                }
                placeholder="Procura por email..."
                className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white text-sm placeholder-slate-400 focus:outline-none focus:border-blue-500"
              />
            </div>

            {/* Start Date Filter */}
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-1">
                Data de Início
              </label>
              <input
                type="datetime-local"
                value={filters.startDate}
                onChange={(e) =>
                  handleFilterChange("startDate", e.target.value)
                }
                className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white text-sm focus:outline-none focus:border-blue-500"
              />
            </div>

            {/* End Date Filter */}
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-1">
                Data de Fim
              </label>
              <input
                type="datetime-local"
                value={filters.endDate}
                onChange={(e) => handleFilterChange("endDate", e.target.value)}
                className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white text-sm focus:outline-none focus:border-blue-500"
              />
            </div>
          </div>

          {/* Filter Buttons */}
          <div className="flex gap-3">
            <button
              onClick={handleApplyFilters}
              disabled={loading}
              className="px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-slate-600 text-white font-medium rounded-lg text-sm transition-colors"
            >
              Aplicar Filtros
            </button>
            <button
              onClick={handleClearFilters}
              className="px-4 py-2 bg-slate-700 hover:bg-slate-600 text-white font-medium rounded-lg text-sm transition-colors"
            >
              Limpar
            </button>
          </div>
        </div>

        {/* Orders Table */}
        <div className="bg-slate-800 border border-slate-700 rounded-xl overflow-hidden">
          {loading && (
            <div className="p-8 text-center text-slate-400">
              A carregar encomendas...
            </div>
          )}

          {!loading && orders.length === 0 && (
            <div className="p-8 text-center text-slate-400">
              Nenhuma encomenda encontrada
            </div>
          )}

          {!loading && orders.length > 0 && (
            <>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-slate-700 border-b border-slate-600">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase">
                        ID da Encomenda
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase">
                        Cliente
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase">
                        Estado
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase">
                        Artigos
                      </th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-slate-300 uppercase">
                        Total
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase">
                        Data
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-700">
                    {orders.map((order) => (
                      <tr
                        key={order.id}
                        className="hover:bg-slate-700/50 transition"
                      >
                        <td className="px-6 py-4 text-sm text-slate-300">
                          {order.id.slice(0, 8)}...
                        </td>
                        <td className="px-6 py-4 text-sm text-slate-300">
                          {order.customerEmail}
                        </td>
                        <td className="px-6 py-4 text-sm">
                          <span
                            className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${getStatusColor(
                              order.status,
                            )}`}
                          >
                            {order.status}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-sm text-slate-300">
                          {order.items.length} item
                          {order.items.length > 1 ? "s" : ""}
                        </td>
                        <td className="px-6 py-4 text-sm font-semibold text-white text-right">
                          €{order.totalAmount.toFixed(2)}
                        </td>
                        <td className="px-6 py-4 text-sm text-slate-400">
                          {new Date(order.createdAt).toLocaleDateString(
                            "pt-PT",
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              <div className="border-t border-slate-700 px-6 py-4 flex items-center justify-between">
                <div className="text-sm text-slate-400">
                  Showing {pagination.currentPage * pagination.pageSize + 1} to{" "}
                  {Math.min(
                    (pagination.currentPage + 1) * pagination.pageSize,
                    pagination.totalElements,
                  )}{" "}
                  of {pagination.totalElements} orders
                </div>

                <div className="flex gap-2">
                  <button
                    onClick={() => handlePageChange(pagination.currentPage - 1)}
                    disabled={pagination.currentPage === 0 || loading}
                    className="px-3 py-1 text-sm bg-slate-700 hover:bg-slate-600 disabled:bg-slate-700 disabled:opacity-50 text-white rounded transition-colors"
                  >
                    ← Previous
                  </button>

                  <div className="flex items-center gap-2">
                    {Array.from({ length: pagination.totalPages }).map(
                      (_, i) => (
                        <button
                          key={i}
                          onClick={() => handlePageChange(i)}
                          className={`px-2 py-1 text-sm rounded transition-colors ${
                            pagination.currentPage === i
                              ? "bg-blue-600 text-white"
                              : "bg-slate-700 hover:bg-slate-600 text-white"
                          }`}
                        >
                          {i + 1}
                        </button>
                      ),
                    )}
                  </div>

                  <button
                    onClick={() => handlePageChange(pagination.currentPage + 1)}
                    disabled={
                      pagination.currentPage >= pagination.totalPages - 1 ||
                      loading
                    }
                    className="px-3 py-1 text-sm bg-slate-700 hover:bg-slate-600 disabled:bg-slate-700 disabled:opacity-50 text-white rounded transition-colors"
                  >
                    Next →
                  </button>
                </div>
              </div>
            </>
          )}
        </div>
      </main>
    </div>
  );
}
