"use client";

import { useEffect, useState } from "react";
import { motion } from "motion/react";
import { QRCodeSVG } from "qrcode.react";
import { Download, Smartphone } from "lucide-react";
import { fetchLatestRelease, type ReleaseInfo } from "@/lib/releases";
import { useI18n } from "@/lib/i18n-context";

export default function DownloadQR() {
  const { t, theme } = useI18n();
  const [release, setRelease] = useState<ReleaseInfo | null>(null);
  const [origin, setOrigin] = useState<string>("");

  useEffect(() => {
    setOrigin(window.location.origin);
    let alive = true;
    fetchLatestRelease().then((r) => {
      if (alive) setRelease(r);
    });
    return () => {
      alive = false;
    };
  }, []);

  const version = release?.version ?? "1.0.0";
  const sizeMb = release?.sizeBytes ? (release.sizeBytes / (1024 * 1024)).toFixed(1) : "<8";
  const downloadUrl = `${origin}/download`;

  const qrBg = theme === "light" ? "#ffffff" : "#0b0f17";
  const qrFg = theme === "light" ? "#007e92" : "#00f0ff";

  return (
    <section id="download" className="mx-auto max-w-6xl scroll-mt-24 px-6 py-24">
      <motion.div
        initial={{ opacity: 0, scale: 0.97 }}
        whileInView={{ opacity: 1, scale: 1 }}
        viewport={{ once: true, margin: "-60px" }}
        transition={{ duration: 0.55 }}
        className="neon-card grid items-center gap-10 rounded-3xl border-electric/20 p-8 sm:p-12 lg:grid-cols-2"
      >
        <div>
          <h2 className="font-display text-3xl font-bold sm:text-4xl">
            {t.download.headingA}
            <br />
            <span className="text-electric text-glow">v{version}</span> · {sizeMb} MB
          </h2>
          <p className="mt-4 max-w-md text-muted">{t.download.desc}</p>
          <div className="mt-8 flex flex-wrap gap-4">
            <a
              href="/download"
              className="inline-flex items-center gap-2 rounded-full bg-electric px-6 py-3 font-semibold text-bg shadow-[0_0_32px_rgba(0,240,255,0.4)] transition-transform hover:scale-105"
            >
              <Download size={18} />
              {t.download.downloadApk}
            </a>
          </div>
          <div className="mt-6 flex items-center gap-2 text-sm text-muted">
            <Smartphone size={15} className="text-electric" />
            {t.download.note}
          </div>
        </div>

        <div className="flex justify-center">
          <div className="rounded-2xl border border-edge bg-surface p-6 shadow-[0_0_48px_rgba(112,0,255,0.25)]">
            <QRCodeSVG
              value={origin ? downloadUrl : "https://picobeam.app"}
              size={208}
              bgColor={qrBg}
              fgColor={qrFg}
              level="M"
            />
            <p className="mt-4 text-center text-sm font-medium text-electric">
              {t.download.scanToDownload.replace("{version}", version)}
            </p>
          </div>
        </div>
      </motion.div>
    </section>
  );
}