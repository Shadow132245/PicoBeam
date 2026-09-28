import type { Metadata } from "next";
import type { ReactNode } from "react";
import "./globals.css";
import Nav from "@/components/Nav";
import Footer from "@/components/Footer";
import { I18nProvider } from "@/lib/i18n-context";

export const metadata: Metadata = {
  title: "PicoBeam — Peer-to-peer file sharing at light speed",
  description:
    "PicoBeam is a tiny (<8MB), ad-free, offline peer-to-peer file transfer app for Android. Up to 80 MB/s over Wi-Fi Direct, plus web-share to any device via QR.",
  keywords: ["picobeam", "file sharing", "wifi direct", "offline transfer", "p2p", "qr share"],
  openGraph: {
    title: "PicoBeam — file sharing at light speed",
    description: "Tiny, ad-free, lightning-fast local peer-to-peer transfers.",
    type: "website",
  },
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className="font-body bg-bg text-ink antialiased min-h-screen">
        <I18nProvider>
          <Nav />
          {children}
          <Footer />
        </I18nProvider>
      </body>
    </html>
  );
}