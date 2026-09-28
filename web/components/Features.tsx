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
import { useI18n } from "@/lib/i18n-context";

const icons = [Wifi, Globe, QrCode, ClipboardCopy, RotateCcw, FolderOpen];

export default function Features() {
  const { t } = useI18n();

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
          {t.features.headingA}
          <span className="text-violet"> {t.features.headingB}</span>
        </h2>
        <p className="mt-4 text-muted">{t.features.sub}</p>
      </motion.div>

      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {t.features.items.map((f, i) => {
          const Icon = icons[i % icons.length];
          return (
            <motion.div
              key={f.title}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, margin: "-40px" }}
              transition={{ duration: 0.45, delay: (i % 3) * 0.08 }}
              className="neon-card group rounded-2xl p-6 transition-transform hover:-translate-y-1"
            >
              <div className="mb-4 inline-flex rounded-xl border border-electric/30 bg-electric/10 p-2.5 transition-transform group-hover:scale-110">
                <Icon size={20} className="text-electric" />
              </div>
              <h3 className="mb-2 text-lg font-semibold">{f.title}</h3>
              <p className="text-sm leading-relaxed text-muted">{f.body}</p>
            </motion.div>
          );
        })}
      </div>
    </section>
  );
}