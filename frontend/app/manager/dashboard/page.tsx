"use client";

import Link from "next/link";

interface DashboardCard {
  title: string;
  description: string;
  href: string;
  available: boolean;
  icon: React.ReactNode;
  badge?: string;
}

const cards: DashboardCard[] = [
  {
    title: "Invite User",
    description: "Send an invitation to a new Manager or Carrier to join the platform.",
    href: "/invite",
    available: true,
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M18 7.5v3m0 0v3m0-3h3m-3 0h-3m-2.25-4.125a3.375 3.375 0 1 1-6.75 0 3.375 3.375 0 0 1 6.75 0ZM3 19.235v-.11a6.375 6.375 0 0 1 12.75 0v.109A12.318 12.318 0 0 1 9.374 21c-2.331 0-4.512-.645-6.374-1.766Z" />
      </svg>
    ),
  },
  {
    title: "Manage Products",
    description: "Create new products and edit existing ones in the catalogue.",
    href: "/products/manage",
    available: true,
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="m21 7.5-9-5.25L3 7.5m18 0-9 5.25m9-5.25v9l-9 5.25M3 7.5l9 5.25M3 7.5v9l9 5.25m0-9v9" />
      </svg>
    ),
  },
  {
    title: "Backups",
    description: "Create a backup of some system data",
    href: "/backups",
    available: true,
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M20.25 6.375c0 2.278-3.694 4.125-8.25 4.125S3.75 8.653 3.75 6.375m16.5 0c0-2.278-3.694-4.125-8.25-4.125S3.75 4.097 3.75 6.375m16.5 0v11.25c0 2.278-3.694 4.125-8.25 4.125s-8.25-1.847-8.25-4.125V6.375m16.5 0v3.75m-16.5-3.75v3.75m16.5 0v3.75C20.25 16.153 16.556 18 12 18s-8.25-1.847-8.25-4.125v-3.75m16.5 0c0 2.278-3.694 4.125-8.25 4.125s-8.25-1.847-8.25-4.125" />
      </svg>
    ),
  },
  {
    title: "Coming Soon",
    description: "More management features will appear here.",
    href: "#",
    available: false,
    icon: (
      <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
      </svg>
    ),
  },
];

export default function DashboardPage() {
  return (
    <div className="py-8">
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">

        {/* Header */}
        <div className="mb-10">
          <p className="text-blue-400 text-sm uppercase tracking-widest mb-1 font-medium">
            Manager Portal
          </p>
          <h1 className="text-4xl font-bold text-white mb-2">Dashboard</h1>
          <p className="text-slate-400">
            Manage your platform - invite users, maintain the catalogue, and keep your data safe.
          </p>
        </div>

        {/* Cards grid */}
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {cards.map((card) =>
            card.available ? (
              <Link
                key={card.title}
                href={card.href}
                className="group relative bg-slate-800 border border-slate-700 rounded-xl p-6 hover:border-blue-500 hover:bg-slate-800/80 transition-all duration-200 flex flex-col gap-4"
              >
                {/* Icon */}
                <div className="w-12 h-12 bg-blue-900/40 text-blue-400 rounded-lg flex items-center justify-center group-hover:bg-blue-900/60 transition">
                  {card.icon}
                </div>

                {/* Text */}
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <h2 className="text-white font-semibold group-hover:text-blue-300 transition">
                      {card.title}
                    </h2>
                    {card.badge && (
                      <span className="text-xs px-2 py-0.5 bg-blue-900/60 text-blue-300 rounded-full border border-blue-700">
                        {card.badge}
                      </span>
                    )}
                  </div>
                  <p className="text-slate-400 text-sm leading-relaxed">{card.description}</p>
                </div>

                {/* Arrow */}
                <svg
                  className="absolute top-6 right-6 w-4 h-4 text-slate-600 group-hover:text-blue-400 transition"
                  fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}
                >
                  <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
                </svg>
              </Link>
            ) : (
              <div
                key={card.title}
                className="bg-slate-800/40 border border-slate-700/40 rounded-xl p-6 opacity-40 cursor-not-allowed flex flex-col gap-4"
              >
                <div className="w-12 h-12 bg-slate-700/40 text-slate-500 rounded-lg flex items-center justify-center">
                  {card.icon}
                </div>
                <div>
                  <h2 className="text-slate-400 font-semibold mb-1">{card.title}</h2>
                  <p className="text-slate-500 text-sm leading-relaxed">{card.description}</p>
                </div>
              </div>
            )
          )}
        </div>
      </main>
    </div>
  );
}