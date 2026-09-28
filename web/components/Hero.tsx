"use client";

import { motion } from "motion/react";
import { Zap, Feather, OctagonX, Download } from "lucide-react";
import Link from "next/link";

const chips = [
  { icon: Zap, label: "80 MB/s real speed" },
  { icon: Feather, label: "under 8 MB apk" },
  { icon: OctagonX, label: "zero ads, zero bloat" },
];

const stats = [
  { value: "80", unit: "MB/s", label: "LAN transfer speed" },
  { value: "<8", unit: "MB", label: "final APK size" },
  { value: "0", unit: "ads", label: "in the entire app" },
];

export default function Hero() {
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
            Offline peer-to-peer, zero cloud
          </p>
          <h1 className="font-display text-5xl font-bold leading-tight tracking-tight sm:text-6xl">
            Send anything
            <br />
            at <span className="text-electric text-glow">light speed.</span>
          </h1>
          <p className="mt-6 max-w-md text-lg text-muted">
            PicoBeam is a featherweight transfer engine: direct device-to-device
            over Wi-Fi, a browser web-share for any screen, and none of the ad
            bloat of legacy apps.
          </p>
          <div className="mt-8 flex flex-wrap gap-4">
            <Link
              href="/download"
              className="inline-flex items-center gap-2 rounded-full bg-electric px-6 py-3 font-semibold text-bg shadow-[0_0_32px_rgba(0,240,255,0.4)] transition-transform hover:scale-105"
            >
              <Download size={18} />
              Download PicoBeam APK
            </Link>
            <Link
              href="#download"
              className="inline-flex items-center gap-2 rounded-full border border-edge px-6 py-3 font-semibold text-ink transition-colors hover:border-electric/60"
            >
              Scan QR on phone
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
          className="relative flex justify-center"
        >
          <motion.img
            src="/icon.svg"
            alt="PicoBeam app icon"
            width={360}
            height={360}
            animate={{ y: [0, -14, 0] }}
            transition={{ duration: 5, repeat: Infinity, ease: "easeInOut" }}
            className="h-60 w-60 rounded-[3rem] drop-shadow-[0_0_48px_rgba(0,240,255,0.35)] sm:h-80 sm:w-80 sm:rounded-[4rem]"
          />
          <div className="absolute -bottom-4 left-1/2 flex -translate-x-1/2 gap-8 rounded-2xl border border-edge bg-surface/90 px-8 py-4 backdrop-blur">
            {stats.map((s) => (
              <div key={s.label} className="text-center">
                <div className="font-display text-2xl font-bold text-electric">
                  {s.value}
                  <span className="ml-1 text-xs text-muted">{s.unit}</span>
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