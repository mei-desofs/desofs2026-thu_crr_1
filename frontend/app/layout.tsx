import "./globals.css";
import Link from "next/link";
import Script from "next/script";
import type { Metadata, Viewport } from "next";

export const metadata: Metadata = {
  title: "TechStore - Secure E-Commerce",
  description:
    "A secure e-commerce platform built with Next.js and Spring Boot",
  appleWebApp: {
    capable: true,
    statusBarStyle: "black-translucent",
  },
  formatDetection: {
    telephone: false,
  },
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
  themeColor: "#000000",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <head>
        {/* Referrer policy */}
        <meta name="referrer" content="strict-origin-when-cross-origin" />

        {/* IE compatibility (legacy, mostly irrelevant in modern Next.js apps) */}
        <meta httpEquiv="X-UA-Compatible" content="ie=edge" />
      </head>

      <body className="bg-slate-900 text-white">
        {/* Security check script (client-side only) */}
        <Script id="security-check" strategy="afterInteractive">
          {`
            (function() {
              const warnings = [];

              // HTTPS check
              if (
                window.location.protocol !== 'https:' &&
                !window.location.hostname.includes('localhost')
              ) {
                warnings.push('HTTPS required: connection is not secure.');
              }

              if (warnings.length > 0) {
                console.warn('Security Check:', warnings.join(' '));
              }
            })();
          `}
        </Script>

        {/* App Shell */}
        <div className="min-h-screen flex flex-col bg-gradient-to-br from-slate-900 to-slate-800 text-white">
          
          {/* HEADER */}
          <header className="bg-slate-950 border-b border-slate-700">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
              <Link
                href="/"
                className="text-2xl font-bold text-white hover:text-blue-400"
              >
                TechStore
              </Link>

              <nav className="flex gap-4">
                <Link
                  href="/cart"
                  className="px-4 py-2 bg-slate-700 text-white rounded hover:bg-slate-600"
                >
                  Cart
                </Link>

                <Link
                  href="/auth"
                  className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                >
                  Sign In
                </Link>
              </nav>
            </div>
          </header>

          {/* MAIN CONTENT (flex grows to push footer down) */}
          <main className="flex-1">
            {children}
          </main>

          {/* FOOTER (sticks to bottom when content is short) */}
          <footer className="mt-auto bg-slate-950 border-t border-slate-700 py-8">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center text-slate-400">
              <p>TechStore © 2026 - All rights reserved.</p>
            </div>
          </footer>
        </div>
      </body>
    </html>
  );
}