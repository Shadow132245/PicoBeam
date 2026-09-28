import { NextResponse } from "next/server";
import { fetchLatestRelease } from "@/lib/releases";

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  const release = await fetchLatestRelease();
  if (release?.apkUrl) {
    return NextResponse.redirect(release.apkUrl, 302);
  }
  const url = new URL(request.url);
  return NextResponse.redirect(`${url.origin}/#download`, 302);
}