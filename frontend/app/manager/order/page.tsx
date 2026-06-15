"use client";

import { useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { apiGet } from "@/lib/api";
import { useToast } from "@/app/components/useToast";

interface ManagerOrder {
  id: string;
  id2: string;
  email: string;
  totalPrice: {
    moneyValue: number;
  };
  string: string;
  createdAt: string;
  size: number;
}

interface OrdersPage {
  content: ManagerOrder[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  numberOfElements: number;
  first: boolean;
  last: boolean;
  empty: boolean;
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
}

type OrderStatus =
  | "PENDING"
  | "PICKED_UP"
  | "SHIPPED"
  | "DELIVERED"
  | "CANCELLED"
  | "";

export default function OrdersManager() {
  const router = useRouter();
  const [orders, setOrders] = useState<ManagerOrder[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    number: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0,
  });
  const { error: showError } = useToast();

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

        if (filters.status) params.append("status", filters.status);
        if (filters.customerEmail) params.append("customerEmail", filters.customerEmail);
        if (filters.startDate) params.append("startDate", filters.startDate);
        if (filters.endDate) params.append("endDate", filters.endDate);

        const data = await apiGet<OrdersPage>(`/orders/manager?${params.toString()}`);

        setOrders(data.content || []);
        setPagination({
          number: data.number,
          size: data.size,
          totalPages: data.totalPages,
          totalElements: data.totalElements,
        });
      } catch (err: any) {
        const errorMsg = err?.response?.data?.error || "Failed to load orders";
        showError(errorMsg);
      } finally {
        setLoading(false);
      }
    },
    [filters, showError],
  );

  useEffect(() => {
    fetchOrders(0);
  }, [fetchOrders]);

  const handleFilterChange = (key: string, value: string) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
  };

  const handleApplyFilters = () => fetchOrders(0);

  const handleClearFilters = () => {
    setFilters({ status: "", customerEmail: "", startDate: "", endDate: "" });
  };

  const handlePageChange = (page: number) => {
    if (page >= 0 && page < pagination.totalPages && page !== pagination.number) {
      fetchOrders(page);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "PENDING":   return "bg-yellow-900/20 text-yellow-200 border-yellow-700";
      case "PICKED_UP": return "bg-blue-900/20 text-blue-200 border-blue-700";
      case "SHIPPED":   return "bg-purple-900/20 text-purple-200 border-purple-700";
      case "DELIVERED": return "bg-green-900/20 text-green-200 border-green-700";
      case "CANCELLED": return "bg-red-900/20 text-red-200 border-red-700";
      default:          return "bg-slate-900/20 text-slate-200 border-slate-700";
    }
  };

  const getStatusTranslation = (status: string): string => {
    if (!status) return "Unknown";
    const translations: Record<string, string> = {
      PENDING: "Pending",
      PICKED_UP: "Picked Up",
      SHIPPED: "Shipped",
      DELIVERED: "Delivered",
      CANCELLED: "Cancelled",
    };
    return translations[status.toUpperCase()] || status;
  };

  const formatDate = (dateString: string): string => {
    try {
      if (!dateString) return "N/A";
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return "N/A";
      return date.toLocaleDateString("en-US");
    } catch {
      return "N/A";
    }
  };

  return (
    <div className="py-8">
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">

        {/* Header */}
        <div className="mb-8">
          <button
            onClick={() => router.push("/manager/dashboard")}
            className="flex items-center gap-2 text-slate-400 hover:text-white transition text-sm mb-4"
          >
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 19.5L8.25 12l7.5-7.5" />
            </svg>
            Back to Dashboard
          </button>
          <h1 className="text-4xl font-bold text-white mb-2">Orders Management</h1>
          <p className="text-slate-400">View and manage customer orders</p>
        </div>

        {/* Filters Card */}
        <div className="bg-slate-800 border border-slate-700 rounded-xl p-6 mb-8">
          <h2 className="text-white font-semibold mb-4">Filters</h2>

          <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
            {/* Status Filter */}
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-1">Status</label>
              <select
                value={filters.status}
                onChange={(e) => handleFilterChange("status", e.target.value)}
                className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white text-sm focus:outline-none focus:border-blue-500"
              >
                <option value="">All Statuses</option>
                <option value="PENDING">Pending</option>
                <option value="PICKED_UP">Picked Up</option>
                <option value="SHIPPED">Shipped</option>
                <option value="DELIVERED">Delivered</option>
                <option value="CANCELLED">Cancelled</option>
              </select>
            </div>

            {/* Email Filter */}
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-1">Customer Email</label>
              <input
                type="email"
                value={filters.customerEmail}
                onChange={(e) => handleFilterChange("customerEmail", e.target.value)}
                placeholder="Search by email..."
                className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white text-sm placeholder-slate-400 focus:outline-none focus:border-blue-500"
              />
            </div>

            {/* Start Date Filter */}
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-1">Start Date</label>
              <input
                type="date"
                value={filters.startDate}
                onChange={(e) => handleFilterChange("startDate", e.target.value)}
                className="w-full px-3 py-2 bg-slate-700 border border-slate-600 rounded-lg text-white text-sm focus:outline-none focus:border-blue-500"
              />
            </div>

            {/* End Date Filter */}
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-1">End Date</label>
              <input
                type="date"
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
              Apply Filters
            </button>
            <button
              onClick={handleClearFilters}
              className="px-4 py-2 bg-slate-700 hover:bg-slate-600 text-white font-medium rounded-lg text-sm transition-colors"
            >
              Clear
            </button>
          </div>
        </div>

        {/* Orders Table */}
        <div className="bg-slate-800 border border-slate-700 rounded-xl overflow-hidden">
          {loading && (
            <div className="p-8 text-center text-slate-400">Loading orders...</div>
          )}

          {!loading && orders.length === 0 && (
            <div className="p-8 text-center text-slate-400">No orders found</div>
          )}

          {!loading && orders.length > 0 && (
            <>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-slate-700 border-b border-slate-600">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase">Order ID</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase">Email</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase">Status</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase">Items</th>
                      <th className="px-6 py-3 text-right text-xs font-medium text-slate-300 uppercase">Total</th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-slate-300 uppercase">Date</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-700">
                    {orders.map((order) => (
                      <tr key={order.id2} className="hover:bg-slate-700/50 transition">
                        <td className="px-6 py-4 text-sm text-slate-300 font-mono">
                          {order.id2?.slice(0, 8)}...
                        </td>
                        <td className="px-6 py-4 text-sm text-slate-300">
                          {order.email || "N/A"}
                        </td>
                        <td className="px-6 py-4 text-sm">
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${getStatusColor(order.string)}`}>
                            {getStatusTranslation(order.string || "")}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-sm text-slate-300">
                          {order.size || 0} item{(order.size || 0) !== 1 ? "s" : ""}
                        </td>
                        <td className="px-6 py-4 text-sm font-semibold text-white text-right">
                          €{(order.totalPrice?.moneyValue || 0).toFixed(2)}
                        </td>
                        <td className="px-6 py-4 text-sm text-slate-400">
                          {formatDate(order.createdAt)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              <div className="border-t border-slate-700 px-6 py-4 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div className="text-sm text-slate-400">
                  Showing{" "}
                  {pagination.totalElements === 0 ? 0 : pagination.number * pagination.size + 1}{" "}
                  to{" "}
                  {Math.min((pagination.number + 1) * pagination.size, pagination.totalElements)}{" "}
                  of {pagination.totalElements} orders
                </div>

                <div className="flex gap-2 flex-wrap">
                  <button
                    onClick={() => handlePageChange(pagination.number - 1)}
                    disabled={pagination.number === 0 || loading}
                    className="px-3 py-1 text-sm bg-slate-700 hover:bg-slate-600 disabled:bg-slate-700 disabled:opacity-50 text-white rounded transition-colors"
                  >
                    ← Previous
                  </button>

                  <div className="flex items-center gap-1">
                    {Array.from({ length: pagination.totalPages }).map((_, i) => (
                      <button
                        key={i}
                        onClick={() => handlePageChange(i)}
                        className={`px-2 py-1 text-sm rounded transition-colors ${
                          pagination.number === i
                            ? "bg-blue-600 text-white"
                            : "bg-slate-700 hover:bg-slate-600 text-white"
                        }`}
                      >
                        {i + 1}
                      </button>
                    ))}
                  </div>

                  <button
                    onClick={() => handlePageChange(pagination.number + 1)}
                    disabled={pagination.number >= pagination.totalPages - 1 || loading}
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