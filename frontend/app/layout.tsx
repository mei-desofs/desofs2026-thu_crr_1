import "./globals.css";
import Script from "next/script";
import type { Metadata, Viewport } from "next";
import NavBar from "./components/NavBar";

import { ToastProvider } from "@/app/components/useToast";
import ToastWrapper from "./components/ToastWrapper";

export const metadata: Metadata = {
  title: "TechStore - Secure E-Commerce",
  description: "A secure e-commerce platform built with Next.js and Spring Boot",
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

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <head>
        <meta name="referrer" content="strict-origin-when-cross-origin" />
        <meta httpEquiv="X-UA-Compatible" content="ie=edge" />
      </head>
      <body className="bg-slate-900 text-white">
        <Script id="security-check" strategy="afterInteractive">
          {`
            (function() {
              if (
                window.location.protocol !== 'https:' &&
                !window.location.hostname.includes('localhost')
              ) {
                console.warn('Security Check: HTTPS required: connection is not secure.');
              }
            })();
          `}
        </Script>
        <ToastProvider>
          <div className="min-h-screen flex flex-col bg-gradient-to-br from-slate-900 to-slate-800 text-white">
           <NavBar />

            <main className="flex-1">
              {children}
            </main>

            <footer className="mt-auto bg-slate-950 border-t border-slate-700 py-8">
              <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center text-slate-400">
                <p>TechStore © 2026 - All rights reserved.</p>
              </div>
            </footer>
          </div>
        <ToastWrapper />
        </ToastProvider>
      </body>
    </html>
  );
}