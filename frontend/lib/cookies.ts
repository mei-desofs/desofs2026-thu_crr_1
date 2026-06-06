/**
 * V3.3: Cookie Setup Security Utilities
 * Handles secure cookie operations with proper attributes
 */

/**
 * V3.3.1, V3.3.2, V3.3.3, V3.3.4: Secure cookie configuration
 * Cookies are set by the backend with:
 * - Secure flag (HTTPS only)
 * - HttpOnly flag (JS cannot access)
 * - SameSite attribute (CSRF protection)
 * - Appropriate prefixes (__Host- or __Secure-)
 * - Maximum size limits (V3.3.5)
 */

export interface CookieOptions {
  maxAge?: number;
  path?: string;
  domain?: string;
  sameSite?: 'Strict' | 'Lax' | 'None';
  secure?: boolean;
  httpOnly?: boolean;
}

/**
 * V3.3.4: Retrieve secure session cookie
 * Session tokens should only be in HttpOnly cookies,
 * never accessed by client-side JavaScript
 */
export function getSessionCookie(): string | null {
  // Session cookie should be HttpOnly on backend
  // This is just a placeholder for documentation
  return null;
}

/**
 * V3.3.1, V3.3.3: Validate cookie naming conventions
 * Cookies should use __Host- or __Secure- prefixes
 */
export function isSecureCookieName(name: string): boolean {
  return name.startsWith('__Host-') || name.startsWith('__Secure-');
}

/**
 * V3.3.5: Validate cookie size
 * Combined cookie name and value must not exceed 4096 bytes
 */
export function validateCookieSize(name: string, value: string): boolean {
  const totalSize = name.length + value.length;
  return totalSize <= 4096;
}

/**
 * Read a cookie value (only for non-HttpOnly cookies)
 * V3.2.2: Safe DOM manipulation to retrieve values
 */
export function getCookie(name: string): string | null {
  if (typeof document === 'undefined') return null;
  
  const nameEQ = name + '=';
  const cookies = document.cookie.split(';');
  
  for (let cookie of cookies) {
    cookie = cookie.trim();
    if (cookie.startsWith(nameEQ)) {
      return decodeURIComponent(cookie.substring(nameEQ.length));
    }
  }
  
  return null;
}

/**
 * Set a cookie with security options
 * V3.3: All cookies should be set with proper security attributes
 */
export function setCookie(
  name: string,
  value: string,
  options: CookieOptions = {}
): void {
  if (typeof document === 'undefined') return;
  
  // Validate size
  if (!validateCookieSize(name, value)) {
    console.error(`Cookie ${name} exceeds 4096 bytes limit`);
    return;
  }
  
  let cookieString = `${encodeURIComponent(name)}=${encodeURIComponent(value)}`;
  
  if (options.maxAge !== undefined) {
    cookieString += `; Max-Age=${options.maxAge}`;
  }
  
  if (options.path) {
    cookieString += `; Path=${options.path}`;
  }
  
  if (options.domain) {
    cookieString += `; Domain=${options.domain}`;
  }
  
  if (options.sameSite) {
    cookieString += `; SameSite=${options.sameSite}`;
  }
  
  if (options.secure) {
    cookieString += '; Secure';
  }
  
  if (options.httpOnly) {
    // Note: httpOnly cannot be set from JavaScript
    // It must be set by the server
    console.warn('HttpOnly cookies must be set by the server');
  }
  
  document.cookie = cookieString;
}

/**
 * Delete a cookie
 */
export function deleteCookie(name: string, path: string = '/'): void {
  setCookie(name, '', {
    maxAge: -1,
    path,
  });
}
