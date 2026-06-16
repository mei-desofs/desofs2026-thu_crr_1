"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { apiGet } from "@/lib/api";

export default function Home() {
  const router = useRouter();

  useEffect(() => {
    // Redirect to products page
    const redirect = async () => {
      const me = await apiGet<{ role: string }>("/auth/me");
      router.refresh();
      if (me.role === "MANAGER") {
        router.push("/manager/dashboard");
      } else if (me.role === "CARRIER") {
        router.push("/carrier");
        } else {
          router.push("/products");
        }
        router.refresh();
    };
    redirect();
  }, [router]);

  return null;
}
