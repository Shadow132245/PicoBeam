"use client";

import { motion } from "motion/react";
import { Zap, Feather, OctagonX, Download, QrCode } from "lucide-react";
import Link from "next/link";
import { useI18n } from "@/lib/i18n-context";

export default function Hero() {
  const { t } = useI18n();
  const chips = [
    { icon: Zap, label: t.hero.chips[0] },
    { icon: Feather, label: t.hero.chips[1] },
    { icon: OctagonX, label: t.hero.chips[2] },
  ];

  return (
    <section className="relative overflow-hidden pb-24 pt-20 sm:pt-28">
      <div className="mx-auto grid max-w-6xl items-center gap-16 px-6 lg:grid-cols-2">
        <motion.div
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, ease: "easeOut" }}
        >
          <p className="mb-4 inline-flex items-center gap-2 rounded-full border border-electric/40 bg-electric/10 px-3 py-1 text-xs font-medium text-electric">
            <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-electric" />
            {t.hero.badge}
          </p>
          <h1 className="font-display text-5xl font-bold leading-tight tracking-tight sm:text-6xl">
            {t.hero.titleA}
            <br />
            <span className="text-electric text-glow">{t.hero.titleAccent}</span>
          </h1>
          <p className="mt-6 max-w-md text-lg text-muted">{t.hero.desc}</p>
          <div className="mt-8 flex flex-wrap gap-4">
            <Link
              href="/download"
              className="inline-flex items-center gap-2 rounded-full bg-electric px-6 py-3 font-semibold text-bg shadow-[0_0_32px_rgba(0,240,255,0.4)] transition-transform hover:scale-105"
            >
              <Download size={18} />
              {t.hero.downloadCta}
            </Link>
            <Link
              href="#download"
              className="inline-flex items-center gap-2 rounded-full border border-edge px-6 py-3 font-semibold text-ink transition-colors hover:border-electric/60"
            >
              <QrCode size={18} className="text-electric" />
              {t.hero.scanCta}
            </Link>
          </div>
          <div className="mt-8 flex flex-wrap gap-3">
            {chips.map((c) => (
              <span
                key={c.label}
                className="inline-flex items-center gap-1.5 rounded-full border border-edge bg-surface/60 px-3 py-1.5 text-sm text-muted"
              >
                <c.icon size={14} className="text-electric" />
                {c.label}
              </span>
            ))}
          </div>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, scale: 0.94 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.7, ease: "easeOut", delay: 0.15 }}
          className="flex flex-col items-center gap-8"
        >
          <motion.div
            animate={{ y: [0, -10, 0] }}
            transition={{ duration: 5, repeat: Infinity, ease: "easeInOut" }}
            className="pointer-events-none"
          >
            <img
              src="/icon.svg"
              alt="PicoBeam app icon"
              width={360}
              height={360}
              className="h-52 w-52 rounded-[2.5rem] drop-shadow-[0_0_48px_rgba(0,240,255,0.35)] sm:h-68 sm:w-68 sm:rounded-[3.25rem]"
            />
          </motion.div>
          <div className="flex flex-wrap justify-center gap-x-8 gap-y-3 rounded-2xl border border-edge bg-surface/90 px-8 py-4 backdrop-blur">
            {t.hero.stats.map((s) => (
              <div key={s.label} className="text-center">
                <div className="font-display text-2xl font-bold text-electric">
                  {s.value}
                  <span className="ms-1 text-xs text-muted">{s.unit}</span>
                </div>
                <div className="text-xs text-muted">{s.label}</div>
              </div>
            ))}
          </div>
        </motion.div>
      </div>
    </section>
  );
}