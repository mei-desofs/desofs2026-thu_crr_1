import { NextRequest, NextResponse } from "next/server";

function decodeJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const parts = token.split(".");
    if (parts.length !== 3) return null;
    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = Buffer.from(base64, "base64").toString("utf-8");
    return JSON.parse(json);
  } catch {
    return null;
  }
}

const MANAGER_ROUTES = ["/manager/dashboard", "/invite", "/manager/backups", "/products/manage"];

function redirectToLogin(request: NextRequest, pathname: string) {
  const loginUrl = request.nextUrl.clone();
  loginUrl.pathname = "/auth/login";
  loginUrl.searchParams.set("redirect", pathname);
  return NextResponse.redirect(loginUrl);
}

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  const accessToken = request.cookies.get("__Secure-access_token")?.value;
  if (!accessToken) return redirectToLogin(request, pathname);

  const payload = decodeJwtPayload(accessToken);
  if (!payload) return redirectToLogin(request, pathname);

  const exp = payload.exp as number | undefined;
  if (exp && Date.now() / 1000 > exp) return redirectToLogin(request, pathname);

  const userMetadata = payload.user_metadata as Record<string, unknown> | undefined;
  const role = (userMetadata?.role as string | undefined)?.toUpperCase();

  const isManagerRoute = MANAGER_ROUTES.some((r) => pathname.startsWith(r));
  if (isManagerRoute && role !== "MANAGER") return redirectToLogin(request, pathname);

  return NextResponse.next();
}

export const config = {
  matcher: [
    "/manager/dashboard/:path*",
    "/invite/:path*",
    "/manager/backups/:path*",
    "/products/manage/:path*",
  ],
};