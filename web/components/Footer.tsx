import Link from "next/link";

export default function Footer() {
  return (
    <footer className="border-t border-edge/60">
      <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-4 px-6 py-8 sm:flex-row">
        <div className="flex items-center gap-2 text-sm text-muted">
          <span className="font-display font-bold text-ink">
            Pico<span className="text-electric">Beam</span>
          </span>
          <span>·</span>
          <span>Made light by design.</span>
        </div>
        <div className="flex items-center gap-6 text-sm text-muted">
          <Link href="#features" className="transition-colors hover:text-electric">
            Features
          </Link>
          <Link href="#compare" className="transition-colors hover:text-electric">
            Comparison
          </Link>
          <Link href="#download" className="transition-colors hover:text-electric">
            Download
          </Link>
        </div>
        <p className="text-xs text-muted/70">
          © {new Date().getFullYear()} PicoBeam · no ads, no tracking, no account.
        </p>
      </div>
    </footer>
  );
}