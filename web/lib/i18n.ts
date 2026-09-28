export type Lang = "en" | "ar" | "fr" | "es";

export const LANGS: { code: Lang; name: string; dir: "ltr" | "rtl" }[] = [
  { code: "en", name: "English", dir: "ltr" },
  { code: "ar", name: "العربية", dir: "rtl" },
  { code: "fr", name: "Français", dir: "ltr" },
  { code: "es", name: "Español", dir: "ltr" },
];

export type Feature = { title: string; body: string };
export type Stat = { value: string; unit: string; label: string };

export type Dict = {
  nav: { features: string; compare: string; download: string; getApk: string };
  hero: {
    badge: string;
    titleA: string;
    titleAccent: string;
    desc: string;
    downloadCta: string;
    scanCta: string;
    chips: [string, string, string];
    stats: Stat[];
  };
  features: { headingA: string; headingB: string; sub: string; items: Feature[] };
  compare: {
    headingA: string;
    headingB: string;
    sub: string;
    metric: string;
    picoColumn: string;
    legacyColumn: string;
    rows: { metric: string; pico: boolean | string; other: boolean | string }[];
    partial: string;
  };
  download: {
    headingA: string;
    desc: string;
    downloadApk: string;
    note: string;
    scanToDownload: string;
  };
  footer: { madeLight: string; rights: string; developedBy: string };
};

export const DICTS: Record<Lang, Dict> = {
  en: {
    nav: { features: "Features", compare: "Comparison", download: "Download", getApk: "Get APK" },
    hero: {
      badge: "Offline peer-to-peer, zero cloud",
      titleA: "Send anything",
      titleAccent: "light speed.",
      desc: "PicoBeam is a featherweight transfer engine: direct device-to-device over Wi-Fi, a browser web-share for any screen, and none of the ad bloat of legacy apps.",
      downloadCta: "Download PicoBeam APK",
      scanCta: "Scan QR on phone",
      chips: ["80 MB/s real speed", "under 8 MB apk", "zero ads, zero bloat"],
      stats: [
        { value: "80", unit: "MB/s", label: "LAN transfer speed" },
        { value: "<8", unit: "MB", label: "final APK size" },
        { value: "0", unit: "ads", label: "in the entire app" },
      ],
    },
    features: {
      headingA: "Everything you need.",
      headingB: "Nothing you don't.",
      sub: "A surgical feature set, engineered for speed and a <8 MB install.",
      items: [
        {
          title: "Offline P2P Transfer",
          body: "Apps, videos, photos and huge files move directly device-to-device over Wi-Fi. No internet, no middle server.",
        },
        {
          title: "Pico Web-Share",
          body: "Your phone becomes a local server. Any device — iPhone, PC, TV — downloads through its browser with a single QR scan.",
        },
        {
          title: "Smart QR & BLE Pairing",
          body: "One instant, secured local session. Point, scan, and the connection is established in a tap.",
        },
        {
          title: "Clipboard Sync",
          body: "Copy a link on one device and paste it on another. Text and URLs travel between devices at a press.",
        },
        {
          title: "Resumable Transfers",
          body: "Interrupted? HTTP Range requests resume exactly where you stopped — never from scratch.",
        },
        {
          title: "Built-in File Manager",
          body: "A featherweight file manager inside the app. Browse and categorize files without leaving the flow.",
        },
      ],
    },
    compare: {
      headingA: "PicoBeam",
      headingB: "vs the bloated rest",
      sub: "Legacy sharing apps ship ads, telemetry and gigabytes of excess. PicoBeam ships one job: transfer.",
      metric: "Metric",
      picoColumn: "PicoBeam",
      legacyColumn: "Legacy apps",
      partial: "partial",
      rows: [
        { metric: "Ad-free", pico: true, other: false },
        { metric: "Offline / no internet", pico: true, other: false },
        { metric: "Speed", pico: "80 MB/s", other: "~15 MB/s" },
        { metric: "Install size", pico: "<8 MB", other: "60–300 MB" },
        { metric: "Web-share to any device", pico: true, other: false },
        { metric: "Resume broken transfers", pico: true, other: "partial" },
        { metric: "Respects your privacy", pico: true, other: false },
      ],
    },
    download: {
      headingA: "Get it now.",
      desc: "Point your phone camera at the QR code, or download the signed APK straight to your device. Updates flow automatically from the release channel.",
      downloadApk: "Download APK",
      note: "Android 8.0+ · signed release · sha-256 pinned on release",
      scanToDownload: "Scan to download {version}",
    },
    footer: { madeLight: "Made light by design.", rights: "no ads, no tracking, no account.", developedBy: "Developed by Hassan — known online as EuroMoscow" },
  },
  ar: {
    nav: { features: "المميزات", compare: "المقارنة", download: "التحميل", getApk: "حمّل التطبيق" },
    hero: {
      badge: "نظير إلى نظير دون إنترنت",
      titleA: "أرسل أي شيء",
      titleAccent: "بسرعة الضوء.",
      desc: "PicoBeam محرك نقل خفيف للغاية: مباشرة من جهاز إلى جهاز عبر الواي فاي، ومشاركة ويب من المتصفح لأي شاشة، وبدون كل إعلانات التطبيقات القديمة.",
      downloadCta: "حمّل تطبيق PicoBeam",
      scanCta: "امسح رمز QR من الهاتف",
      chips: ["سرعة حقيقية 80 MB/s", "حجم أقل من 8 MB", "صفر إعلانات وصفر إضافات"],
      stats: [
        { value: "80", unit: "MB/s", label: "سرعة النقل على الشبكة" },
        { value: "<8", unit: "MB", label: "حجم التطبيق النهائي" },
        { value: "0", unit: "إعلان", label: "داخل التطبيق بالكامل" },
      ],
    },
    features: {
      headingA: "كل ما تحتاجه.",
      headingB: "ولا شيء أكثر.",
      sub: "مجموعة ميزات جراحية، مصمّمة للسرعة مع تثبيت أقل من 8 MB.",
      items: [
        {
          title: "نقل مباشر بين الأجهزة",
          body: "التطبيقات والفيديوهات والصور والملفات الكبيرة تنتقل مباشرة بين الأجهزة عبر الواي فاي. بدون إنترنت وبدون خادم وسيط.",
        },
        {
          title: "مشاركة الويب Pico",
          body: "هاتفك يصبح خادماً محلياً. أي جهاز — آيفون أو حاسوب أو تلفاز — يحمّل عبر متصفحه بمسح رمز QR واحد.",
        },
        {
          title: "اقتران ذكي بالرمز والبلوتوث",
          body: "جلسة محلية فورية وآمنة. وجّه وامسح، وتُقام الاتصال بلمسة واحدة.",
        },
        {
          title: "مزامنة الحافظة",
          body: "انسخ رابطاً على جهاز والصقه على آخر. النصوص والروابط تنتقل بين الأجهزة بضغطة واحدة.",
        },
        {
          title: "استئناف النقل",
          body: "انقطع النقل؟ طلبات HTTP Range تستأنف من النقطة نفسها التي توقفت عندها — لا تبدأ من الصفر أبداً.",
        },
        {
          title: "إدارة ملفات مدمجة",
          body: "مدير ملفات خفيف داخل التطبيق. تصفّح ملفاتك وصنّفها دون مغادرة التجربة.",
        },
      ],
    },
    compare: {
      headingA: "PicoBeam",
      headingB: "ضدّ غيرهم الممتلئين",
      sub: "تطبيقات المشاركة القديمة مليئة بالإعلانات والتتبع وغَيغابايت الزيادة. PicoBeam مهمته واحدة: النقل.",
      metric: "البند",
      picoColumn: "PicoBeam",
      legacyColumn: "التطبيقات القديمة",
      partial: "جزئي",
      rows: [
        { metric: "بدون إعلانات", pico: true, other: false },
        { metric: "يعمل دون إنترنت", pico: true, other: false },
        { metric: "السرعة", pico: "80 MB/s", other: "~15 MB/s" },
        { metric: "حجم التثبيت", pico: "<8 MB", other: "60–300 MB" },
        { metric: "مشاركة الويب لأي جهاز", pico: true, other: false },
        { metric: "استئناف النقل المتقطع", pico: true, other: "partial" },
        { metric: "يحترم خصوصيتك", pico: true, other: false },
      ],
    },
    download: {
      headingA: "حمّله الآن.",
      desc: "وجّه كاميرا هاتفك إلى رمز QR، أو حمّل التطبيق الموقّع مباشرة على جهازك. التحديثات تصل تلقائياً من قناة الإصدارات.",
      downloadApk: "تحميل التطبيق",
      note: "أندرويد 8.0+ · إصدار موقّع · بصمة sha-256 مثبتة عند الإصدار",
      scanToDownload: "امسح لتحميل {version}",
    },
    footer: { madeLight: "خفيف بالتصميم.", rights: "لا إعلانات ولا تتبع ولا حساب.", developedBy: "تطوير Hassan — المعروف إلكترونياً باسم EuroMoscow" },
  },
  fr: {
    nav: { features: "Fonctionnalités", compare: "Comparaison", download: "Télécharger", getApk: "Obtenir l'APK" },
    hero: {
      badge: "Pair-à-pair hors ligne, zéro cloud",
      titleA: "Envoyez tout",
      titleAccent: "à la vitesse de la lumière.",
      desc: "PicoBeam est un moteur de transfert plume : directement d'appareil à appareil via Wi-Fi, un web-share navigateur pour tout écran, et aucun des bloat publicitaire des apps héritées.",
      downloadCta: "Télécharger l'APK PicoBeam",
      scanCta: "Scanner le QR sur le téléphone",
      chips: ["80 MB/s réels", "APK de moins de 8 MB", "zéro pub, zéro superflu"],
      stats: [
        { value: "80", unit: "MB/s", label: "vitesse LAN" },
        { value: "<8", unit: "MB", label: "taille finale de l'APK" },
        { value: "0", unit: "pub", label: "dans toute l'app" },
      ],
    },
    features: {
      headingA: "Tout ce qu'il faut.",
      headingB: "Rien de superflu.",
      sub: "Un ensemble de fonctions chirurgical, conçu pour la vitesse et une installation <8 MB.",
      items: [
        {
          title: "Transfert P2P hors ligne",
          body: "Apps, vidéos, photos et gros fichiers passent directement d'appareil à appareil via Wi-Fi. Sans internet, sans serveur intermédiaire.",
        },
        {
          title: "Pico Web-Share",
          body: "Votre téléphone devient un serveur local. N'importe quel appareil — iPhone, PC, TV — télécharge via son navigateur avec un simple scan QR.",
        },
        {
          title: "Appairage QR & BLE intelligent",
          body: "Une session locale instantanée et sécurisée. Pointez, scannez, la connexion s'établit en une tap.",
        },
        {
          title: "Synchronisation du presse-papiers",
          body: "Copiez un lien sur un appareil et collez-le sur un autre. Textes et URL voyagent entre appareils en une pression.",
        },
        {
          title: "Transferts reprenables",
          body: "Interrompu ? Les requêtes HTTP Range reprennent exactement là où vous vous êtes arrêté — jamais à zéro.",
        },
        {
          title: "Gestionnaire de fichiers intégré",
          body: "Un gestionnaire de fichiers plume à l'intérieur de l'app. Parcourez et classez sans quitter le flux.",
        },
      ],
    },
    compare: {
      headingA: "PicoBeam",
      headingB: "contre le reste gonflé",
      sub: "Les apps de partage héritées embarquent pubs, télémétrie et gigaoctets superflus. PicoBeam a une seule mission : transférer.",
      metric: "Critère",
      picoColumn: "PicoBeam",
      legacyColumn: "Apps héritées",
      partial: "partiel",
      rows: [
        { metric: "Sans publicité", pico: true, other: false },
        { metric: "Hors ligne / sans internet", pico: true, other: false },
        { metric: "Vitesse", pico: "80 MB/s", other: "~15 MB/s" },
        { metric: "Taille d'installation", pico: "<8 MB", other: "60–300 MB" },
        { metric: "Web-share vers tout appareil", pico: true, other: false },
        { metric: "Reprise des transferts", pico: true, other: "partial" },
        { metric: "Respecte votre vie privée", pico: true, other: false },
      ],
    },
    download: {
      headingA: "Obtenez-le maintenant.",
      desc: "Pointez l'appareil photo sur le QR, ou téléchargez l'APK signé directement. Les mises à jour arrivent automatiquement depuis le canal de publication.",
      downloadApk: "Télécharger l'APK",
      note: "Android 8.0+ · version signée · sha-256 épinglé à la release",
      scanToDownload: "Scannez pour télécharger {version}",
    },
    footer: { madeLight: "Léger par conception.", rights: "pas de pub, pas de suivi, pas de compte.", developedBy: "Développé par Hassan — connu en ligne sous le nom d'EuroMoscow" },
  },
  es: {
    nav: { features: "Funciones", compare: "Comparación", download: "Descargar", getApk: "Obtener APK" },
    hero: {
      badge: "Punto a punto sin conexión, cero nube",
      titleA: "Envía cualquier cosa",
      titleAccent: "a la velocidad de la luz.",
      desc: "PicoBeam es un motor de transferencia pluma: directo de dispositivo a dispositivo por Wi-Fi, un web-share en el navegador para cualquier pantalla y nada del bloat publicitario de las apps antiguas.",
      downloadCta: "Descarga el APK de PicoBeam",
      scanCta: "Escanea el QR en el teléfono",
      chips: ["80 MB/s reales", "APK de menos de 8 MB", "cero anuncios, cero bloat"],
      stats: [
        { value: "80", unit: "MB/s", label: "velocidad LAN" },
        { value: "<8", unit: "MB", label: "tamaño final del APK" },
        { value: "0", unit: "anuncios", label: "en toda la app" },
      ],
    },
    features: {
      headingA: "Todo lo que necesitas.",
      headingB: "Nada de sobra.",
      sub: "Un conjunto de funciones quirúrgico, diseñado para velocidad y una instalación <8 MB.",
      items: [
        {
          title: "Transferencia P2P sin conexión",
          body: "Apps, vídeos, fotos y archivos grandes pasan directamente de dispositivo a dispositivo por Wi-Fi. Sin internet, sin servidor intermedio.",
        },
        {
          title: "Pico Web-Share",
          body: "Tu teléfono se convierte en un servidor local. Cualquier dispositivo — iPhone, PC, TV — descarga desde su navegador con un solo escaneo QR.",
        },
        {
          title: "Emparejamiento QR y BLE inteligente",
          body: "Una sesión local instantánea y segura. Apunta, escanea y la conexión se establece con un toque.",
        },
        {
          title: "Sincronización del portapapeles",
          body: "Copia un enlace en un dispositivo y pégalo en otro. Textos y URL viajan entre dispositivos con un toque.",
        },
        {
          title: "Transferencias reanudables",
          body: "¿Se interrumpió? Las peticiones HTTP Range reanudan justo donde te detuviste — nunca desde cero.",
        },
        {
          title: "Gestor de archivos integrado",
          body: "Un gestor de archivos pluma dentro de la app. Explora y organiza sin salir del flujo.",
        },
      ],
    },
    compare: {
      headingA: "PicoBeam",
      headingB: "frente al resto inflado",
      sub: "Las apps de intercambio antiguas traen anuncios, telemetría y gigabytes de más. PicoBeam tiene un solo cometido: transferir.",
      metric: "Criterio",
      picoColumn: "PicoBeam",
      legacyColumn: "Apps antiguas",
      partial: "parcial",
      rows: [
        { metric: "Sin anuncios", pico: true, other: false },
        { metric: "Sin conexión / sin internet", pico: true, other: false },
        { metric: "Velocidad", pico: "80 MB/s", other: "~15 MB/s" },
        { metric: "Tamaño de instalación", pico: "<8 MB", other: "60–300 MB" },
        { metric: "Web-share a cualquier dispositivo", pico: true, other: false },
        { metric: "Reanudar transferencias", pico: true, other: "partial" },
        { metric: "Respeta tu privacidad", pico: true, other: false },
      ],
    },
    download: {
      headingA: "Consíguelo ahora.",
      desc: "Apunta la cámara del teléfono al QR o descarga el APK firmado directamente. Las actualizaciones llegan solas desde el canal de releases.",
      downloadApk: "Descargar APK",
      note: "Android 8.0+ · versión firmada · sha-256 fijado en la release",
      scanToDownload: "Escanea para descargar {version}",
    },
    footer: { madeLight: "Ligero por diseño.", rights: "sin anuncios, sin rastreo, sin cuenta.", developedBy: "Desarrollado por Hassan — conocido en línea como EuroMoscow" },
  },
};