"use client";

import { useState } from "react";
import Link from "next/link";
import { AnimatePresence, motion } from "motion/react";
import { ChevronDown, Globe, Moon, Sun } from "lucide-react";
import { LANGS } from "@/lib/i18n";
import { useI18n } from "@/lib/i18n-context";

export default function Nav() {
  const { t, lang, setLang, theme, toggleTheme } = useI18n();
  const [open, setOpen] = useState(false);

  const links = [
    { href: "#features", label: t.nav.features },
    { href: "#compare", label: t.nav.compare },
    { href: "#download", label: t.nav.download },
  ];

  return (
    <header className="sticky top-0 z-50 border-b border-edge/60 bg-bg/80 backdrop-blur-md">
      <nav className="mx-auto flex h-16 max-w-6xl items-center justify-between gap-3 px-6">
        <Link href="/" className="flex shrink-0 items-center gap-2.5">
          <img src="/icon.svg" alt="PicoBeam" width={28} height={28} className="rounded-lg" />
          <span className="font-display text-lg font-bold tracking-tight">
            Pico<span className="text-electric text-glow">Beam</span>
          </span>
        </Link>

        <div className="hidden items-center gap-8 text-sm text-muted lg:flex">
          {links.map((l) => (
            <Link key={l.href} href={l.href} className="transition-colors hover:text-electric">
              {l.label}
            </Link>
          ))}
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={toggleTheme}
            aria-label={theme === "dark" ? "Switch to light mode" : "Switch to dark mode"}
            className="inline-flex h-9 w-9 items-center justify-center rounded-full border border-edge text-ink transition-colors hover:border-electric/60"
          >
            <AnimatePresence mode="wait" initial={false}>
              <motion.span
                key={theme}
                initial={{ rotate: -90, scale: 0.4, opacity: 0 }}
                animate={{ rotate: 0, scale: 1, opacity: 1 }}
                exit={{ rotate: 90, scale: 0.4, opacity: 0 }}
                transition={{ duration: 0.25 }}
              >
                {theme === "dark" ? <Sun size={16} /> : <Moon size={16} />}
              </motion.span>
            </AnimatePresence>
          </button>

          <div className="relative">
            <button
              type="button"
              onClick={() => setOpen((o) => !o)}
              className="inline-flex h-9 items-center gap-1.5 rounded-full border border-edge px-3 text-sm text-ink transition-colors hover:border-electric/60"
              aria-haspopup="listbox"
              aria-expanded={open}
            >
              <Globe size={15} className="text-electric" />
              <span className="hidden sm:inline">
                {LANGS.find((l) => l.code === lang)?.name}
              </span>
              <ChevronDown
                size={13}
                className={`text-muted transition-transform duration-200 ${open ? "rotate-180" : ""}`}
              />
            </button>
            <AnimatePresence>
              {open && (
                <motion.ul
                  role="listbox"
                  initial={{ opacity: 0, scale: 0.92, y: -6 }}
                  animate={{ opacity: 1, scale: 1, y: 0 }}
                  exit={{ opacity: 0, scale: 0.92, y: -6 }}
                  transition={{ duration: 0.16, ease: "easeOut" }}
                  className="absolute end-0 top-full z-50 mt-2 w-36 overflow-hidden rounded-xl border border-edge bg-surface/95 p-1 text-sm shadow-2xl backdrop-blur"
                >
                  {LANGS.map((l) => (
                    <li key={l.code}>
                      <button
                        type="button"
                        role="option"
                        aria-selected={lang === l.code}
                        onClick={() => {
                          setLang(l.code);
                          setOpen(false);
                        }}
                        className={`flex w-full items-center justify-between rounded-lg px-3 py-2 text-ink transition-colors hover:bg-electric/10 hover:text-electric ${
                          lang === l.code ? "text-electric" : ""
                        }`}
                      >
                        <span>
                          {l.code === "ar" ? "العربية" : l.name} · {l.code.toUpperCase()}
                        </span>
                      </button>
                    </li>
                  ))}
                </motion.ul>
              )}
            </AnimatePresence>
          </div>

          <Link
            href="/download"
            className="rounded-full bg-electric px-4 py-1.5 text-sm font-semibold text-bg transition-transform hover:scale-105"
          >
            {t.nav.getApk}
          </Link>
        </div>
      </nav>
    </header>
  );
}