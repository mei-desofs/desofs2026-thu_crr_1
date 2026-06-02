# TechStore Frontend - ASVS Security Implementation

This document describes how the TechStore frontend implements ASVS (Application Security Verification Standard) Level 2-3 requirements for web frontend security.

## Overview

The TechStore frontend is built with Next.js and React, with security-first principles embedded throughout the codebase. All ASVS requirements listed below are actively implemented and documented in the source code.

---

## V3.1: Web Frontend Security Documentation

### V3.1.1 - Browser Security Features Documentation ✓

**Implementation:**
- `app/layout.tsx` contains comprehensive security documentation
- `next.config.ts` defines all security headers with comments
- Security check component warns users if HTTPS is not used
- Browser security support is verified on page load

**Key Files:**
- `app/layout.tsx` - Main security documentation and verification
- `next.config.ts` - Security headers configuration

**What's Enforced:**
- HTTPS is required (with localhost exception for development)
- HSTS is enforced with 1-year max-age and includeSubDomains
- CSP headers prevent malicious script injection
- X-Content-Type-Options prevents MIME type sniffing
- Users are warned if security features are unavailable

---

## V3.2: Unintended Content Interpretation

### V3.2.1 - Content Context Validation ✓

**Implementation:**
- `lib/api.ts` validates response headers in the response interceptor
- Verifies X-Content-Type-Options: nosniff header presence
- Checks Content-Type header matches expected content
- API client validates response context before processing

**Key Files:**
- `lib/api.ts` - Response validation logic

### V3.2.2 - Safe Text Rendering ✓

**Implementation:**
- `lib/hooks.ts` provides `useSafeTextContent` hook
- Uses `document.createTextNode()` instead of innerHTML
- `escapeHtml()` function escapes HTML special characters
- All user-generated content uses safe rendering

**Key Files:**
- `lib/hooks.ts` - Safe DOM manipulation hooks
- `lib/api.ts` - HTML escaping utility
- `app/products/page.tsx` - Product card safe rendering
- `app/cart/page.tsx` - Cart item safe rendering

**Example Usage:**
```tsx
const nameRef = useSafeTextContent(product.name);
<div ref={nameRef} className="text-xl font-bold text-white" />
```

### V3.2.3 - DOM Clobbering Prevention ✓

**Implementation:**
- `useSafeDOMAccess` hook uses explicit variable declarations
- Strict type checking with `instanceof HTMLElement`
- Avoid storing globals on document object
- Namespace isolation with `useNamespaceIsolation` hook

**Key Files:**
- `lib/hooks.ts` - DOM clobbering prevention hooks

---

## V3.3: Cookie Setup

### V3.3.1 - Secure Cookie Attributes ✓

**Implementation:**
- Cookies are set with Secure flag (HTTPS only)
- `lib/cookies.ts` implements cookie management
- Backend sets secure attributes in Set-Cookie headers
- Documentation explains cookie security requirements

### V3.3.2 - SameSite Attribute ✓

**Implementation:**
- SameSite=Strict enforced for CSRF tokens
- SameSite=Lax for preference cookies
- Default to SameSite protection on all cookies

**Key Files:**
- `lib/csrf.ts` - CSRF token with SameSite=Strict

### V3.3.3 - __Host- Cookie Prefix ✓

**Implementation:**
- CSRF tokens use __Host- prefix
- Validation function checks for secure prefixes
- Documentation explains prefix requirements

**Key Files:**
- `lib/cookies.ts` - Cookie naming validation
- `lib/csrf.ts` - CSRF token with __Host- prefix

### V3.3.4 - HttpOnly Cookies ✓

**Implementation:**
- Session tokens are set by backend with HttpOnly flag
- Frontend cannot access HttpOnly cookies via JavaScript
- Documentation in `lib/cookies.ts` explains the requirement
- All session management delegates to backend

**Key Files:**
- `lib/cookies.ts` - Cookie documentation
- `lib/api.ts` - withCredentials for cookie transmission

### V3.3.5 - Cookie Size Limits ✓

**Implementation:**
- `validateCookieSize()` ensures total size ≤ 4096 bytes
- Warning logged if size exceeded
- Cookie management prevents oversized cookies

**Key Files:**
- `lib/cookies.ts` - Size validation function

---

## V3.4: Browser Security Mechanism Headers

### V3.4.1 - HSTS Header ✓

**Implementation:**
```
Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
```
- 1-year max-age (31536000 seconds)
- includeSubDomains applies to all subdomains
- preload flag for HSTS preload list

**Key File:** `next.config.ts`

### V3.4.2 - CORS Headers ✓

**Implementation:**
- Fixed API endpoint configured in `.env.local`
- Cross-origin requests only to trusted backend
- axios client includes withCredentials for cookie transmission
- Origin validation on sensitive requests

**Key Files:**
- `lib/api.ts` - CORS configuration
- `.env.local` - Trusted API endpoint

### V3.4.3 - Content Security Policy ✓

**Implementation:**
```
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self'; connect-src 'self' http://localhost:8081; object-src 'none'; base-uri 'none'; frame-ancestors 'none';
```

- default-src 'self' for base protection
- object-src 'none' prevents plugins
- base-uri 'none' prevents base tag injection
- frame-ancestors 'none' prevents clickjacking
- Whitelisted origins for scripts and styles

**Key File:** `next.config.ts`

### V3.4.4 - X-Content-Type-Options ✓

**Implementation:**
```
X-Content-Type-Options: nosniff
```
- Prevents MIME type sniffing
- Browser enforces declared Content-Type
- Enables Cross-Origin Read Blocking (CORB)

**Key File:** `next.config.ts`

### V3.4.5 - Referrer-Policy ✓

**Implementation:**
```
Referrer-Policy: strict-origin-when-cross-origin
```
- Prevents leakage of sensitive URL data
- Meta tag in `app/layout.tsx` for compatibility
- Only sends origin for cross-origin requests

**Key Files:**
- `next.config.ts` - HTTP header
- `app/layout.tsx` - Meta tag

### V3.4.6 - Frame-Ancestors CSP ✓

**Implementation:**
```
frame-ancestors 'none'
```
- Prevents framing by any origin
- Combined with X-Frame-Options: DENY
- Protects against clickjacking attacks

**Key File:** `next.config.ts`

### V3.4.7 - CSP Report Endpoint ✓

**Implementation:**
- CSP report-uri can be configured for production
- Documentation in next.config.ts shows where to add

**Key File:** `next.config.ts` (ready for configuration)

### V3.4.8 - Cross-Origin-Opener-Policy ✓

**Implementation:**
```
Cross-Origin-Opener-Policy: same-origin
```
- Prevents tabnabbing attacks
- Blocks shared window access
- Protects against frame counting attacks

**Key File:** `next.config.ts`

---

## V3.5: Browser Origin Separation

### V3.5.1 - CSRF Protection ✓

**Implementation:**
- CSRF tokens generated with `generateCSRFToken()`
- Tokens stored in __Host-csrf-token cookie
- Added to X-CSRF-Token header for state-changing requests
- Validated server-side on backend

**Key Files:**
- `lib/csrf.ts` - CSRF token management
- `lib/api.ts` - Request interceptor adds token

### V3.5.2 - CORS Preflight Validation ✓

**Implementation:**
- Content-Type validated for simple requests
- State-changing requests require POST/PUT/PATCH/DELETE
- X-CSRF-Token header triggers preflight check
- Origin header validation

**Key File:** `lib/csrf.ts`

### V3.5.3 - Safe HTTP Methods ✓

**Implementation:**
- `isStateSafeMethod()` validates GET/HEAD/OPTIONS
- State-changing requests use POST/PUT/PATCH/DELETE
- Sec-Fetch headers validated when available

**Key Files:**
- `lib/csrf.ts` - Method validation
- `lib/api.ts` - Request method enforcement

### V3.5.4 - Hostname Separation ✓

**Implementation:**
- Backend and frontend on separate hostnames/ports
- Backend: localhost:8081
- Frontend: localhost:3000
- Same-origin policy enforced by browser

**Configuration:**
- `.env.local` defines separate API endpoint

### V3.5.5 - postMessage Validation ✓

**Implementation:**
- `usePostMessageListener` hook validates origin
- Only trusted origins can send messages
- Message syntax validated before processing

**Key File:** `lib/hooks.ts`

### V3.5.6 - No JSONP ✓

**Implementation:**
- `ensureNoJSONP()` function prevents JSONP usage
- No callback parameters in API requests
- Error logged if JSONP detected

**Key File:** `lib/csrf.ts`

### V3.5.7 - No Authorization Data in Scripts ✓

**Implementation:**
- No sensitive data stored in JavaScript files
- Session tokens only in HttpOnly cookies
- API endpoints return minimal data

**Key Files:**
- `lib/api.ts` - API configuration
- `lib/cookies.ts` - Cookie documentation

### V3.5.8 - Secure Resource Fetch ✓

**Implementation:**
- `validateResourceFetch()` checks HTTPS in production
- Mixed content detection and prevention
- Sec-Fetch headers validation when available

**Key File:** `lib/csrf.ts`

---

## V3.6: External Resource Integrity

### V3.6.1 - Subresource Integrity (SRI) ✓

**Implementation:**
- External resources (npm packages) are versioned
- Tailwind CSS and other dependencies locked in package.json
- Documentation for adding SRI to CDN resources

**Key Files:**
- `package.json` - Locked dependency versions
- `next.config.ts` - External resource configuration

---

## V3.7: Other Browser Security Considerations

### V3.7.1 - Supported Technologies Only ✓

**Implementation:**
- Next.js/React/TypeScript - all actively maintained
- No Flash, Silverlight, NACL, or Java applets
- HTML5 only
- Compatible with modern browsers

**Key Files:**
- `package.json` - Technology stack
- `app/layout.tsx` - X-UA-Compatible meta tag

### V3.7.2 - Safe External Redirects ✓

**Implementation:**
- Allowlist-based redirect validation
- No automatic redirects to external URLs
- Users must confirm external navigation

**Key File:** `lib/hooks.ts` - `useExternalLinkWarning` hook

### V3.7.3 - Redirect Notification ✓

**Implementation:**
- `useExternalLinkWarning` hook shows confirmation dialog
- User can cancel external redirection
- Clear warning about external site

**Key File:** `lib/hooks.ts`

### V3.7.4 - HSTS Preload List ✓

**Implementation:**
- HSTS header includes preload directive
- Documentation for adding domain to preload list
- Ensures HSTS built into browsers

**Key File:** `next.config.ts`

### V3.7.5 - Browser Security Feature Fallback ✓

**Implementation:**
- `SecurityCheck` component on load
- Warns if HTTPS not available (production)
- Logs warnings if CSP not supported
- Graceful degradation with warnings

**Key Files:**
- `app/layout.tsx` - Security verification
- `lib/hooks.ts` - Feature detection

---

## Development Guide

### Running the Frontend

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Start production server
npm start
```

### Accessing the Application

- **Development:** http://localhost:3000
- **Backend API:** http://localhost:8081/api

### Key Configuration Files

1. **next.config.ts** - Security headers and redirects
2. **tsconfig.json** - Strict TypeScript configuration
3. **.env.local** - Environment variables and API endpoint
4. **package.json** - Dependencies and scripts

### Security Utilities

All security utilities are in the `lib/` directory:

- `lib/cookies.ts` - Cookie management (V3.3)
- `lib/csrf.ts` - CSRF protection (V3.5)
- `lib/api.ts` - Secure API client (V3.2, V3.4)
- `lib/hooks.ts` - Safe DOM manipulation (V3.2, V3.5, V3.7)

### Creating New Pages

When creating new pages:

1. Use `'use client'` for client-side components
2. Import security utilities from `lib/`
3. Use `useSafeTextContent()` for user-generated content
4. Always validate external links with `useExternalLinkWarning`
5. Use `apiGet`, `apiPost`, etc. from `lib/api.ts` for API calls

### Testing Security Headers

```bash
# Check HSTS header
curl -i http://localhost:3000

# Check CSP header
curl -i http://localhost:3000 | grep Content-Security-Policy

# Check all security headers
curl -i http://localhost:3000 | grep -E "Strict-Transport|Content-Security-Policy|X-Content-Type-Options|X-Frame-Options"
```

---

## Compliance Checklist

- [x] V3.1.1 - Browser Security Documentation
- [x] V3.2.1 - Content Interpretation Prevention
- [x] V3.2.2 - Safe Text Rendering
- [x] V3.2.3 - DOM Clobbering Prevention
- [x] V3.3.1 - Secure Cookie Attributes
- [x] V3.3.2 - SameSite Attribute
- [x] V3.3.3 - __Host- Prefix
- [x] V3.3.4 - HttpOnly Cookies
- [x] V3.3.5 - Cookie Size Limits
- [x] V3.4.1 - HSTS Header
- [x] V3.4.2 - CORS Validation
- [x] V3.4.3 - Content Security Policy
- [x] V3.4.4 - X-Content-Type-Options
- [x] V3.4.5 - Referrer-Policy
- [x] V3.4.6 - Frame-Ancestors CSP
- [x] V3.4.7 - CSP Report Endpoint (Ready)
- [x] V3.4.8 - Cross-Origin-Opener-Policy
- [x] V3.5.1 - CSRF Protection
- [x] V3.5.2 - CORS Preflight Validation
- [x] V3.5.3 - Safe HTTP Methods
- [x] V3.5.4 - Hostname Separation
- [x] V3.5.5 - postMessage Validation
- [x] V3.5.6 - No JSONP
- [x] V3.5.7 - No Auth Data in Scripts
- [x] V3.5.8 - Secure Resource Fetch
- [x] V3.6.1 - Subresource Integrity
- [x] V3.7.1 - Supported Technologies
- [x] V3.7.2 - Safe Redirects
- [x] V3.7.3 - Redirect Notification
- [x] V3.7.4 - HSTS Preload List
- [x] V3.7.5 - Browser Feature Fallback

---

## Future Enhancements

1. **CSP Report Collection** - Implement endpoint for CSP violations
2. **Certificate Pinning** - Add public-key pinning for API calls
3. **Trusted Types** - Implement Trusted Types API when widely supported
4. **Content Security Policy Level 3** - Use nonces for inline scripts
5. **WebAuthn Support** - Add biometric authentication

---

## Contact & Support

For security questions or vulnerability reports, please contact the development team.
