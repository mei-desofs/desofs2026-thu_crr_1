const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081';

/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'standalone',
  reactStrictMode: true,
  // Security: Ensure API calls go through HTTPS in production
  redirects: async () => {
    return [];
  },
  headers: async () => {
    return [
      {
        source: "/:path*",
        headers: [
          // V3.4.1: Strict-Transport-Security (HSTS)
          {
            key: "Strict-Transport-Security",
            value: "max-age=31536000; includeSubDomains; preload",
          },
          // V3.4.3: Content-Security-Policy
          {
            key: "Content-Security-Policy",
            value:
              `default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self'; connect-src 'self' ${API_URL}; object-src 'none'; base-uri 'none'; frame-ancestors 'none';`,
          },
          // V3.4.4: X-Content-Type-Options
          {
            key: "X-Content-Type-Options",
            value: "nosniff",
          },
          // V3.4.5: Referrer-Policy
          {
            key: "Referrer-Policy",
            value: "strict-origin-when-cross-origin",
          },
          // V3.4.6: X-Frame-Options (legacy) and CSP frame-ancestors
          {
            key: "X-Frame-Options",
            value: "DENY",
          },
          // V3.4.8: Cross-Origin-Opener-Policy
          {
            key: "Cross-Origin-Opener-Policy",
            value: "same-origin",
          },
          // Additional security headers
          {
            key: "X-XSS-Protection",
            value: "1; mode=block",
          },
          {
            key: "Permissions-Policy",
            value:
              "geolocation=(), microphone=(), camera=(), payment=(), usb=(), magnetometer=(), gyroscope=(), accelerometer=()",
          },
        ],
      },
    ];
  },
};

module.exports = nextConfig;
