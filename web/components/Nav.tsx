import Link from "next/link";

const links = [
  { href: "#features", label: "Features" },
  { href: "#compare", label: "Comparison" },
  { href: "#download", label: "Download" },
];

export default function Nav() {
  return (
    <header className="sticky top-0 z-50 border-b border-edge/60 bg-bg/80 backdrop-blur-md">
      <nav className="mx-auto flex h-16 max-w-6xl items-center justify-between px-6">
        <Link href="/" className="flex items-center gap-2.5">
          <img src="/icon.svg" alt="PicoBeam" width={28} height={28} className="rounded-lg" />
          <span className="font-display text-lg font-bold tracking-tight">
            Pico<span className="text-electric text-glow">Beam</span>
          </span>
        </Link>
        <div className="hidden items-center gap-8 text-sm text-muted sm:flex">
          {links.map((l) => (
            <Link key={l.href} href={l.href} className="transition-colors hover:text-electric">
              {l.label}
            </Link>
          ))}
        </div>
        <Link
          href="/download"
          className="rounded-full bg-electric px-4 py-1.5 text-sm font-semibold text-bg transition-transform hover:scale-105"
        >
          Get APK
        </Link>
      </nav>
    </header>
  );
}