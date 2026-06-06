# TechStore Frontend

A secure, modern e-commerce frontend built with **Next.js** and **React**, implementing ASVS (Application Security Verification Standard) Level 2-3 requirements for comprehensive web frontend security.

## Features

### 🔒 Security-First Architecture

- **HTTPS/HSTS** - Enforced with 1-year max-age and HSTS preload support
- **Content Security Policy (CSP)** - Strict CSP headers prevent XSS and malicious script injection
- **CSRF Protection** - Token-based CSRF validation on all state-changing requests
- **Secure Cookies** - HttpOnly, Secure, and SameSite attributes with size limits
- **Safe DOM Manipulation** - Text content rendering prevents XSS attacks
- **XSS Prevention** - HTML escaping and safe text rendering utilities
- **CORS Security** - Validated origin checking and safe cross-origin requests
- **Secure Headers** - X-Content-Type-Options, X-Frame-Options, Referrer-Policy, etc.

### 📱 Modern Tech Stack

- **Next.js 14** - React framework with App Router
- **TypeScript** - Strict type checking for type safety
- **Tailwind CSS** - Utility-first CSS framework
- **Axios** - HTTP client with security interceptors
- **ESLint** - Code quality and consistency

### 🎯 Core Features

- Browse products with search and pagination
- Shopping cart management
- Responsive design for all devices
- Secure API integration with backend
- Session management with secure cookies
- Rate limiting support

## Quick Start

### Prerequisites

- Node.js 18+ and npm
- TechStore Backend running on http://localhost:8081

### Installation

```bash
# Clone or navigate to the frontend directory
cd frontend

# Install dependencies
npm install

# Create/update environment variables
cp .env.local.example .env.local

# Start development server
npm run dev
```

The application will be available at `http://localhost:3000`

## Project Structure

```
frontend/
├── app/                          # Next.js App Router
│   ├── layout.tsx               # Root layout with security configuration
│   ├── page.tsx                 # Home page
│   ├── globals.css              # Global styles
│   ├── products/
│   │   └── page.tsx             # Products listing page
│   └── cart/
│       └── page.tsx             # Shopping cart page
├── lib/                          # Security and utility libraries
│   ├── api.ts                   # Secure API client with interceptors
│   ├── cookies.ts               # Cookie management (V3.3)
│   ├── csrf.ts                  # CSRF protection (V3.5)
│   └── hooks.ts                 # Safe DOM manipulation hooks (V3.2, V3.5)
├── next.config.ts               # Security headers configuration
├── tsconfig.json                # TypeScript configuration
├── tailwind.config.ts           # Tailwind CSS configuration
├── package.json                 # Dependencies
├── SECURITY.md                  # Detailed security documentation
└── README.md                     # This file
```

## Security Implementation

### ASVS Compliance

This frontend implements all ASVS Level 2-3 requirements for web frontend security:

- **V3.1** - Web Frontend Security Documentation
- **V3.2** - Unintended Content Interpretation Prevention
- **V3.3** - Secure Cookie Setup
- **V3.4** - Browser Security Mechanism Headers
- **V3.5** - Browser Origin Separation
- **V3.6** - External Resource Integrity
- **V3.7** - Other Browser Security Considerations

See [SECURITY.md](./SECURITY.md) for detailed implementation of each requirement.

### Security Headers

The application enforces the following security headers:

```
Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; ...
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Referrer-Policy: strict-origin-when-cross-origin
Cross-Origin-Opener-Policy: same-origin
```

### CSRF Protection

All state-changing requests (POST, PUT, PATCH, DELETE) include a CSRF token:

```typescript
// CSRF token is automatically added to requests
const response = await apiPost('/api/orders', orderData);
```

### Safe API Client

The Axios-based API client includes:

- Automatic CSRF token injection
- Response header validation
- Error handling without information disclosure
- Secure cookie transmission with `withCredentials`

## Available Scripts

```bash
# Development server with hot reload
npm run dev

# Build for production
npm run build

# Start production server
npm start

# Run ESLint
npm run lint
```

## Environment Variables

Create a `.env.local` file with:

```env
# API Configuration
NEXT_PUBLIC_API_URL=http://localhost:8081/api

# Security
NEXT_PUBLIC_CSRF_ENABLED=true
```

## API Integration

The frontend communicates with the TechStore backend:

- **Products API** - `GET /products`, `GET /products/search`
- **Cart API** - Manages shopping cart locally with persistence
- **Auth** - Supabase integration (when implemented)

## Browser Support

- Chrome/Edge 90+
- Firefox 88+
- Safari 14+
- Mobile browsers (iOS Safari, Chrome Android)

## Common Development Tasks

### Adding a New Page

1. Create directory: `app/new-feature/`
2. Create page: `app/new-feature/page.tsx`
3. Use security utilities from `lib/`
4. Use `useSafeTextContent()` for dynamic content
5. Use `apiGet`, `apiPost` for API calls

### Making API Requests

```typescript
import { apiGet, apiPost } from '@/lib/api';

// GET request
const products = await apiGet('/products');

// POST request with CSRF protection
const order = await apiPost('/orders', orderData);
```

### Rendering User Content Safely

```typescript
import { useSafeTextContent } from '@/lib/hooks';

function MyComponent({ userText }) {
  const ref = useSafeTextContent(userText);
  return <div ref={ref} />;
}
```

## Testing

### Manual Testing

1. Start backend: `cd backend && ./mvnw spring-boot:run`
2. Start frontend: `npm run dev`
3. Open http://localhost:3000
4. Navigate and test features

### Security Headers Check

```bash
# Verify HSTS header
curl -I http://localhost:3000 | grep Strict-Transport-Security

# Verify CSP header
curl -I http://localhost:3000 | grep Content-Security-Policy

# Check all headers
curl -I http://localhost:3000
```

## Troubleshooting

### API Connection Issues

- Ensure backend is running on `http://localhost:8081`
- Check `.env.local` has correct `NEXT_PUBLIC_API_URL`
- Verify CORS is configured on backend

### TypeScript Errors

```bash
npm run lint
```

### Build Errors

```bash
npm run build
```

## Performance Optimization

- Image optimization (Next.js Image component)
- Code splitting with dynamic imports
- CSS minification with Tailwind
- JavaScript minification in production builds

## Accessibility

- Semantic HTML elements
- ARIA labels where needed
- Keyboard navigation support
- Color contrast compliance

## Contributing

1. Create a new branch for your feature
2. Follow security best practices (see SECURITY.md)
3. Use safe DOM manipulation utilities
4. Add TypeScript types
5. Test thoroughly

## License

MIT License - See LICENSE file

## Support

For issues, security concerns, or feature requests, please contact the development team.

---

**Built with security in mind** 🔒
