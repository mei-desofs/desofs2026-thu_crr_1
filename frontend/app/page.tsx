"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function Home() {
  const router = useRouter();

  useEffect(() => {
    const redirect = async () => {
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/auth/me`,
          {
            credentials: "include",
          }
        );

        if (!response.ok) {
          router.replace("/products");
          return;
        }

        const me = await response.json();

        if (me.role === "MANAGER") {
          router.replace("/manager/dashboard");
        } else if (me.role === "CARRIER") {
          router.replace("/carrier");
        } else {
          router.replace("/products");
        }
      } catch {
        router.replace("/products");
      }
    };

    redirect();
  }, [router]);

  return null;
}