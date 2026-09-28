"use client";

import { useEffect, useState } from "react";
import { motion } from "motion/react";
import { QRCodeSVG } from "qrcode.react";
import { Download, Smartphone, Play } from "lucide-react";
import { fetchLatestRelease, type ReleaseInfo } from "@/lib/releases";

const PLAY_STORE_URL =
  process.env.NEXT_PUBLIC_PLAY_STORE_URL ?? "https://play.google.com/store/apps/details?id=com.picobeam";

export default function DownloadQR() {
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
            Get it now.
            <br />
            <span className="text-electric text-glow">v{version}</span> · {sizeMb} MB
          </h2>
          <p className="mt-4 max-w-md text-muted">
            Point your phone camera at the QR code, or download the signed APK
            straight to your device. Updates flow automatically from the release
            channel.
          </p>
          <div className="mt-8 flex flex-wrap gap-4">
            <a
              href="/download"
              className="inline-flex items-center gap-2 rounded-full bg-electric px-6 py-3 font-semibold text-bg shadow-[0_0_32px_rgba(0,240,255,0.4)] transition-transform hover:scale-105"
            >
              <Download size={18} />
              Download APK
            </a>
            <a
              href={PLAY_STORE_URL}
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-2 rounded-full border border-edge px-6 py-3 font-semibold text-ink transition-colors hover:border-electric/60"
            >
              <Play size={18} className="text-electric" />
              Google Play
            </a>
          </div>
          <div className="mt-6 flex items-center gap-2 text-sm text-muted">
            <Smartphone size={15} className="text-electric" />
            Android 8.0+ · signed release · sha-256 pinned on release
          </div>
        </div>

        <div className="flex justify-center">
          <div className="rounded-2xl border border-edge bg-bg p-6 shadow-[0_0_48px_rgba(112,0,255,0.25)]">
            <QRCodeSVG
              value={origin ? downloadUrl : "https://picobeam.app"}
              size={208}
              bgColor="#0b0f17"
              fgColor="#00f0ff"
              level="M"
            />
            <p className="mt-4 text-center text-sm font-medium text-electric">
              Scan to download {version}
            </p>
          </div>
        </div>
      </motion.div>
    </section>
  );
}