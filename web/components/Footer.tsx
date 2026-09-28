"use client";

import Link from "next/link";
import { useI18n } from "@/lib/i18n-context";

export default function Footer() {
  const { t } = useI18n();

  const links = [
    { href: "#features", label: t.nav.features },
    { href: "#compare", label: t.nav.compare },
    { href: "#download", label: t.nav.download },
  ];

  return (
    <footer className="border-t border-edge/60">
      <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-4 px-6 py-8 sm:flex-row">
        <div className="flex items-center gap-2 text-sm text-muted">
          <span className="font-display font-bold text-ink">
            Pico<span className="text-electric">Beam</span>
          </span>
          <span>·</span>
          <span>{t.footer.madeLight}</span>
        </div>
        <div className="flex items-center gap-6 text-sm text-muted">
          {links.map((l) => (
            <Link key={l.href} href={l.href} className="transition-colors hover:text-electric">
              {l.label}
            </Link>
          ))}
        </div>
        <p className="text-xs text-muted/70">
          © {new Date().getFullYear()} EuroMoscow Developments · {t.footer.rights}
        </p>
      </div>
      <p className="pb-6 text-center text-xs text-muted/60">{t.footer.developedBy}</p>
    </footer>
  );
}