import { cookies } from "next/headers";
import Link from "next/link";
import LogoutButton from "./LogoutButton";

function decodeJwt(token: string): Record<string, unknown> | null {
  try {
    const parts = token.split(".");
    if (parts.length !== 3) return null;
    return JSON.parse(
      Buffer.from(parts[1].replace(/-/g, "+").replace(/_/g, "/"), "base64").toString("utf-8")
    );
  } catch {
    return null;
  }
}
function getRole(payload: Record<string, unknown>): string | null {
  const meta = payload["user_metadata"];
  if (meta && typeof meta === "object" && "role" in meta) {
    const role = (meta as Record<string, unknown>)["role"];
    if (typeof role === "string") return role.toUpperCase();
  }
  return null;
}

export default async function NavBar() {
  const cookieStore = await cookies();
  const accessToken = cookieStore.get("__Secure-access_token")?.value;

  let isLoggedIn = false;
  let role: string | null = null;

  if (accessToken) {
    const payload = decodeJwt(accessToken);
    if (payload) {
      const exp = payload.exp as number | undefined;
      isLoggedIn = !exp || Date.now() / 1000 < exp;
       if (isLoggedIn) {
        role = getRole(payload);
      }
    }
  }

  return (
    <header className="bg-slate-950 border-b border-slate-700">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
        <Link href="/" className="text-2xl font-bold text-white hover:text-blue-400 transition">
          TechStore
        </Link>

        <nav className="flex gap-4 items-center">
          {isLoggedIn ? (
            <>
              {role === "CUSTOMER" && (
                <>
                  <Link
                    href="/cart"
                    className="px-4 py-2 bg-slate-700 text-white rounded hover:bg-slate-600 transition"
                  >
                    Cart
                  </Link>
                  <Link
                      href="/orders"
                      className="px-4 py-2 bg-slate-700 text-white rounded hover:bg-slate-600 transition"
                    >
                      My Orders
                    </Link>
                  </>
                )}
                {role === "CARRIER" && (
                  <Link
                    href="/carrier/orders"
                    className="px-4 py-2 bg-slate-700 text-white rounded hover:bg-slate-600 transition"
                  >
                    My Deliveries
                  </Link>
                )}
                {role === "CARRIER" && (
                  <Link
                    href="/carrier"
                    className="px-4 py-2 bg-slate-700 text-white rounded hover:bg-slate-600 transition"
                  >
                    Orders
                  </Link>
                )}
                
                {role === "MANAGER" && (
                  <Link
                    href="/manager/dashboard"
                    className="px-4 py-2 bg-slate-700 text-white rounded hover:bg-slate-600 transition"
                  >
                    Dashboard
                  </Link>
                )}
                <Link
                  href="/settings/mfa"
                  className="px-4 py-2 bg-slate-700 text-white rounded hover:bg-slate-600 transition"
                >
                  Security
                </Link>
                <LogoutButton />
              </>
            ) : (
              <>
                <Link
                  href="/auth/login"
                  className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
                >
                  Sign In
                </Link>
                <Link
                  href="/auth/register"
                  className="px-4 py-2 bg-slate-700 text-white rounded hover:bg-slate-600 transition"
                >
                  Sign Up
                </Link>
              </>
          )}
        </nav>
      </div>
    </header>
  );
}