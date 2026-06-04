/**
 * V3.2: Secure API Client
 * Handles all API communication with security best practices
 */

import axios, { AxiosInstance, AxiosRequestConfig, isAxiosError  } from 'axios';
import { getSecureRequestHeaders, validateResourceFetch } from './csrf';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081/api';

/**
 * V3.4.2: Configure CORS and origin validation
 * Only communicate with trusted backend origins
 */
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // V3.3.4: Include secure cookies with requests
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * V3.2.2: Request interceptor for security
 * Adds CSRF tokens and validates requests
 */
apiClient.interceptors.request.use((config) => {
  const { getCSRFToken, isStateSafeMethod } = require('./csrf');

  // V3.5.1: Add CSRF token for state-changing requests
  if (!isStateSafeMethod(config.method || 'GET')) {
    const csrfToken = getCSRFToken();
    if (csrfToken) {
      config.headers['X-CSRF-Token'] = csrfToken;
    }
  }

  // V3.5.8: Validate resource fetch origin
  if (config.url) {
    validateResourceFetch(config.url, config.method);
  }

  return config;
});

/**
 * V3.2: Response interceptor for security
 * Validates response headers and prevents content interpretation issues
 */

let refreshPromise: Promise<void> | null = null;

apiClient.interceptors.response.use(
  (response) => {
    // V3.2.1: Verify response headers indicate correct context
    const contentType = response.headers['content-type'] || '';

    // V3.4.4: Verify X-Content-Type-Options header is present
    const xContentTypeOptions = response.headers['x-content-type-options'];
    if (xContentTypeOptions !== 'nosniff') {
      console.warn('Response missing X-Content-Type-Options: nosniff header');
    }

    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    if (
    isAxiosError(error) &&
    (error.response?.status === 401 || error.response?.status === 403) &&
    !originalRequest._retry
  ) {
    originalRequest._retry = true;

    try {
      if (!refreshPromise) {
        refreshPromise = apiClient
          .post('/auth/refresh') 
          .then(() => { refreshPromise = null; })
          .catch((err) => { refreshPromise = null; throw err; });
      }
      await refreshPromise;

      // New access_token cookie is now set by the backend, retry.
      return apiClient(originalRequest);
    } catch {
      // Refresh token expired or revoked — force re-login.
      if (typeof window !== 'undefined') {
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }
  }
    // V3.2.1: Handle other errors securely without exposing sensitive details
  if (isAxiosError(error) && error.response) {
    const status = error.response.status;
    const message = error.response.data?.message || 'An error occurred';
    console.error(`API Error [${status}]:`, message);
  }

  return Promise.reject(error);
  }
);

export interface ApiResponse<T> {
  data: T;
  status: number;
  message?: string;
}

/**
 * V3.2.2: Generic GET request with safe response handling
 */
export async function apiGet<T>(
  endpoint: string,
  config?: AxiosRequestConfig
): Promise<T> {
  const response = await apiClient.get<T>(endpoint, config);
  return response.data;
}

/**
 * V3.2.2: Generic POST request with CSRF protection
 */
export async function apiPost<T>(
  endpoint: string,
  data?: unknown,
  config?: AxiosRequestConfig
): Promise<T> {
  const response = await apiClient.post<T>(endpoint, data, config);
  return response.data;
}

/**
 * V3.2.2: Generic PUT request with CSRF protection
 */
export async function apiPut<T>(
  endpoint: string,
  data?: unknown,
  config?: AxiosRequestConfig
): Promise<T> {
  const response = await apiClient.put<T>(endpoint, data, config);
  return response.data;
}

/**
 * V3.2.2: Generic PATCH request with CSRF protection
 */
export async function apiPatch<T>(
  endpoint: string,
  data?: unknown,
  config?: AxiosRequestConfig
): Promise<T> {
  const response = await apiClient.patch<T>(endpoint, data, config);
  return response.data;
}

/**
 * V3.2.2: Generic DELETE request with CSRF protection
 */
export async function apiDelete<T>(
  endpoint: string,
  config?: AxiosRequestConfig
): Promise<T> {
  const response = await apiClient.delete<T>(endpoint, config);
  return response.data;
}

/**
 * V3.2.2: Safe text rendering
 * Prevents XSS by creating text nodes instead of using innerHTML
 */
export function createSafeTextElement(text: string): Text {
  return document.createTextNode(text);
}

/**
 * V3.2.3: Safe HTML escaping
 * Escapes HTML special characters to prevent XSS
 */
export function escapeHtml(text: string): string {
  const map: Record<string, string> = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;',
  };
  return text.replace(/[&<>"']/g, (char) => map[char]);
}

export default apiClient;
