"use client";

import { motion } from "motion/react";
import { Check, X, Minus } from "lucide-react";
import { useI18n } from "@/lib/i18n-context";

function Cell({ v, partial }: { v: boolean | string; partial: string }) {
  if (v === true) return <Check size={18} className="mx-auto text-electric" />;
  if (v === false) return <X size={18} className="mx-auto text-muted/60" />;
  if (v === "partial") return <Minus size={18} className="mx-auto text-muted/60" />;
  return <span className="font-medium">{v}</span>;
}

export default function Compare() {
  const { t } = useI18n();

  return (
    <section id="compare" className="mx-auto max-w-4xl scroll-mt-24 px-6 py-24">
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-80px" }}
        transition={{ duration: 0.5 }}
        className="mb-10 text-center"
      >
        <h2 className="font-display text-3xl font-bold sm:text-4xl">
          {t.compare.headingA}{" "}
          <span className="text-electric text-glow">{t.compare.headingB}</span>
        </h2>
        <p className="mx-auto mt-3 max-w-xl text-muted">{t.compare.sub}</p>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 24 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-60px" }}
        transition={{ duration: 0.55 }}
        className="neon-card overflow-hidden rounded-2xl"
      >
        <div className="overflow-x-auto">
          <table className="w-full min-w-[540px] text-sm">
            <thead>
              <tr className="border-b border-edge/70 text-start">
                <th className="px-6 py-4 text-start text-muted font-medium">
                  {t.compare.metric}
                </th>
                <th className="border-x border-edge/70 bg-electric/10 px-6 py-4 text-center font-display text-base text-electric">
                  {t.compare.picoColumn}
                </th>
                <th className="px-6 py-4 text-center text-muted font-medium">
                  {t.compare.legacyColumn}
                </th>
              </tr>
            </thead>
            <tbody>
              {t.compare.rows.map((r) => (
                <tr key={r.metric} className="border-b border-edge/40 last:border-0">
                  <td className="px-6 py-3.5 text-start text-muted">{r.metric}</td>
                  <td className="border-x border-edge/70 bg-electric/5 px-6 py-3.5 text-center">
                    <Cell v={r.pico} partial={t.compare.partial} />
                  </td>
                  <td className="px-6 py-3.5 text-center">
                    <Cell v={r.other} partial={t.compare.partial} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </motion.div>
    </section>
  );
}