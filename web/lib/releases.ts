export type ReleaseInfo = {
  tag: string;
  version: string;
  apkUrl: string;
  sizeBytes: number | null;
  publishedAt: string;
};

const OWNER = process.env.NEXT_PUBLIC_REPO_OWNER ?? "";
const REPO = process.env.NEXT_PUBLIC_REPO_NAME ?? "";

export function isGitHubConfigured(): boolean {
  return OWNER.length > 0 && REPO.length > 0;
}

export async function fetchLatestRelease(): Promise<ReleaseInfo | null> {
  if (!isGitHubConfigured()) return null;
  try {
    const res = await fetch(
      `https://api.github.com/repos/${OWNER}/${REPO}/releases/latest`,
      { headers: { Accept: "application/vnd.github+json" }, next: { revalidate: 600 } },
    );
    if (!res.ok) return null;
    const data = (await res.json()) as {
      tag_name: string;
      published_at: string;
      assets: Array<{ name: string; browser_download_url: string; size: number }>;
    };
    const apk = data.assets.find((a) => a.name.toLowerCase().endsWith(".apk"));
    if (!apk) return null;
    return {
      tag: data.tag_name,
      version: data.tag_name.replace(/^v/, ""),
      apkUrl: apk.browser_download_url,
      sizeBytes: apk.size,
      publishedAt: data.published_at,
    };
  } catch {
    return null;
  }
}