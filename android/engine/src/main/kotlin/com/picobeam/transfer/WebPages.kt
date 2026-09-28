package com.picobeam.transfer

import kotlinx.serialization.json.Json

/**
 * Lightweight HTML served to browsers (Pico Web-Share) so any device can
 * download the session files without installing anything.
 */
internal object WebPages {

    private val json = Json { encodeDefaults = true }

    private const val PAGE = """
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>PicoBeam — %DEVICE%</title>
<style>
  :root { --bg:#0b0f17; --card:#111826; --edge:#1c2740; --el:#00f0ff; --ink:#e7edf7; --muted:#8a94a6; }
  * { box-sizing:border-box; margin:0; padding:0; }
  body { background:var(--bg); color:var(--ink); font-family:system-ui,-apple-system,Segoe UI,Roboto,sans-serif; min-height:100vh; }
  main { max-width:640px; margin:0 auto; padding:48px 20px; }
  .head { display:flex; align-items:center; gap:12px; margin-bottom:8px; }
  .dot { width:10px; height:10px; border-radius:50%; background:var(--el); box-shadow:0 0 12px var(--el); }
  h1 { font-size:20px; font-weight:700; }
  .sub { color:var(--muted); font-size:14px; margin-bottom:28px; }
  ul { list-style:none; display:grid; gap:10px; }
  li a { display:flex; align-items:center; justify-content:space-between; gap:12px;
         background:var(--card); border:1px solid var(--edge); border-radius:14px;
         padding:14px 16px; color:var(--ink); text-decoration:none; transition:border-color .15s; }
  li a:hover { border-color:var(--el); }
  .name { font-size:15px; word-break:break-all; }
  .meta { color:var(--muted); font-size:13px; white-space:nowrap; }
  .empty { color:var(--muted); text-align:center; padding:40px 0; }
  footer { text-align:center; color:var(--muted); font-size:12px; margin-top:32px; }
</style>
</head>
<body>
<main>
  <div class="head"><span class="dot"></span><h1>%DEVICE% · PicoBeam Web-Share</h1></div>
  <p class="sub">%COUNT% file(s) ready. Tap to download over your local network.</p>
  <div id="files">Loading…</div>
  <footer>PicoBeam — local transfer, zero cloud.</footer>
</main>
<script>
fetch('session').then(r=>r.json()).then(s=>{
  const el=document.getElementById('files');
  if(!s.files || s.files.length===0){ el.innerHTML='<div class="empty">No files in this session</div>'; return; }
  el.innerHTML='<ul>'+s.files.map(f=>
    '<li><a href="./file?id='+encodeURIComponent(f.id)+'"><span class="name">'+f.name+'</span>'+
    '<span class="meta">'+humanSize(f.size)+'</span></a></li>'
  ).join('')+'</ul>';
});
function humanSize(b){ if(b<1024)return b+' B'; const u=['KB','MB','GB','TB']; let i=-1; do{b/=1024;i++;}while(b>=1024&&i<u.length-1); return b.toFixed(1)+' '+u[i]; }
</script>
</body>
</html>
"""

    fun index(source: FileSource): String = PAGE
        .replace("%DEVICE%", htmlEscape(source.deviceName))
        .replace("%COUNT%", source.entries.size.toString())

    fun json(session: SessionInfo): String = json.encodeToString(SessionInfo.serializer(), session)

    private fun htmlEscape(s: String): String = buildString(s.length) {
        for (c in s) {
            when (c) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                else -> append(c)
            }
        }
    }
}