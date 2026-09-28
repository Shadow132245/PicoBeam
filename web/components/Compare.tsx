"use client";

import { motion } from "motion/react";
import { Check, X, Minus } from "lucide-react";

const rows = [
  { metric: "Ad-free", pico: true, other: false },
  { metric: "Offline / no internet", pico: true, other: false },
  { metric: "Speed", pico: "80 MB/s", other: "~15 MB/s" },
  { metric: "Install size", pico: "<8 MB", other: "60–300 MB" },
  { metric: "Web-share to any device", pico: true, other: false },
  { metric: "Resume broken transfers", pico: true, other: "partial" },
  { metric: "Respects your privacy", pico: true, other: false },
];

function Cell({ v }: { v: boolean | string }) {
  if (v === true) return <Check size={18} className="mx-auto text-electric" />;
  if (v === false) return <X size={18} className="mx-auto text-muted/60" />;
  if (v === "partial")
    return <Minus size={18} className="mx-auto text-muted/60" />;
  return <span className="font-medium">{v}</span>;
}

export default function Compare() {
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
          PicoBeam <span className="text-electric text-glow">vs</span> the bloated
          rest
        </h2>
        <p className="mx-auto mt-3 max-w-xl text-muted">
          Legacy sharing apps ship ads, telemetry and gigabytes of excess.
          PicoBeam ships one job: transfer.
        </p>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 24 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, margin: "-60px" }}
        transition={{ duration: 0.55 }}
        className="neon-card overflow-hidden rounded-2xl"
      >
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-edge/70 text-left">
              <th className="px-6 py-4 text-muted">Metric</th>
              <th className="border-x border-edge/70 bg-electric/10 px-6 py-4 text-center font-display text-base text-electric">
                PicoBeam
              </th>
              <th className="px-6 py-4 text-center text-muted">Legacy apps</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.metric} className="border-b border-edge/40 last:border-0">
                <td className="px-6 py-3.5 text-muted">{r.metric}</td>
                <td className="border-x border-edge/70 bg-electric/5 px-6 py-3.5 text-center">
                  <Cell v={r.pico} />
                </td>
                <td className="px-6 py-3.5 text-center">
                  <Cell v={r.other} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </motion.div>
    </section>
  );
}