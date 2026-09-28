"use client";

import { motion } from "motion/react";
import {
  Wifi,
  Globe,
  QrCode,
  ClipboardCopy,
  RotateCcw,
  FolderOpen,
} from "lucide-react";

const features = [
  {
    icon: Wifi,
    title: "Offline P2P Transfer",
    body: "Apps, videos, photos and huge files move directly device-to-device over Wi-Fi. No internet, no middle server.",
  },
  {
    icon: Globe,
    title: "Pico Web-Share",
    body: "Your phone becomes a local server. Any device — iPhone, PC, TV — downloads through its browser with a single QR scan.",
  },
  {
    icon: QrCode,
    title: "Smart QR & BLE Pairing",
    body: "One instant, secured local session. Point, scan, and the connection is established in a tap.",
  },
  {
    icon: ClipboardCopy,
    title: "Clipboard Sync",
    body: "Copy a link on one device and paste it on another. Text and URLs travel between devices at a press.",
  },
  {
    icon: RotateCcw,
    title: "Resumable Transfers",
    body: "Interrupted? HTTP Range requests resume exactly where you stopped — never from scratch.",
  },
  {
    icon: FolderOpen,
    title: "Built-in Light Manager",
    body: "A featherweight file manager inside the app. Browse and categorize files without leaving the flow.",
  },
];

export default function Features() {
  return (
    <section id="features" className="mx-auto max-w-6xl scroll-mt-24 px-6 py-24">
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-80px" }}
        transition={{ duration: 0.5 }}
        className="mb-14 max-w-2xl"
      >
        <h2 className="font-display text-3xl font-bold sm:text-4xl">
          Everything you need.
          <span className="text-violet"> Nothing you don&apos;t.</span>
        </h2>
        <p className="mt-4 text-muted">
          A surgical feature set, engineered for speed and a <b>&lt;8 MB</b> install.
        </p>
      </motion.div>

      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {features.map((f, i) => (
          <motion.div
            key={f.title}
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true, margin: "-40px" }}
            transition={{ duration: 0.45, delay: (i % 3) * 0.08 }}
            className="neon-card group rounded-2xl p-6 transition-transform hover:-translate-y-1"
          >
            <div className="mb-4 inline-flex rounded-xl border border-electric/30 bg-electric/10 p-2.5 transition-transform group-hover:scale-110">
              <f.icon size={20} className="text-electric" />
            </div>
            <h3 className="mb-2 text-lg font-semibold">{f.title}</h3>
            <p className="text-sm leading-relaxed text-muted">{f.body}</p>
          </motion.div>
        ))}
      </div>
    </section>
  );
}