"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { AnimatePresence, motion } from "motion/react";
import { DICTS, LANGS, type Dict, type Lang } from "@/lib/i18n";

type Theme = "dark" | "light";

type I18nContextValue = {
  lang: Lang;
  dir: "ltr" | "rtl";
  t: Dict;
  setLang: (l: Lang) => void;
  theme: Theme;
  toggleTheme: () => void;
};

const I18nContext = createContext<I18nContextValue | null>(null);

function getInitial<T>(key: string, fallback: T, validate: (v: string) => boolean): T {
  if (typeof window === "undefined") return fallback;
  try {
    const raw = window.localStorage.getItem(key);
    return raw && validate(raw) ? (raw as T) : fallback;
  } catch {
    return fallback;
  }
}

export function I18nProvider({ children }: { children: ReactNode }) {
  const [lang, setLangState] = useState<Lang>("en");
  const [theme, setThemeState] = useState<Theme>("dark");

  useEffect(() => {
    setLangState(getInitial<Lang>("pb.lang", "en", (v) => LANGS.some((l) => l.code === v)));
    setThemeState(getInitial<Theme>("pb.theme", "dark", (v) => v === "dark" || v === "light"));
  }, []);

  useEffect(() => {
    const dir = LANGS.find((l) => l.code === lang)?.dir ?? "ltr";
    document.documentElement.lang = lang;
    document.documentElement.dir = dir;
    try {
      window.localStorage.setItem("pb.lang", lang);
    } catch {}
  }, [lang]);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    try {
      window.localStorage.setItem("pb.theme", theme);
    } catch {}
  }, [theme]);

  const setLang = useCallback((l: Lang) => setLangState(l), []);
  const toggleTheme = useCallback(
    () => setThemeState((t) => (t === "dark" ? "light" : "dark")),
    [],
  );

  const value = useMemo<I18nContextValue>(
    () => ({
      lang,
      dir: LANGS.find((l) => l.code === lang)?.dir ?? "ltr",
      t: DICTS[lang],
      setLang,
      theme,
      toggleTheme,
    }),
    [lang, setLang, theme, toggleTheme],
  );

  return (
    <I18nContext.Provider value={value}>
      <AnimatePresence mode="wait" initial={false}>
        <motion.div
          key={lang}
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -8 }}
          transition={{ duration: 0.25, ease: "easeOut" }}
        >
          {children}
        </motion.div>
      </AnimatePresence>
    </I18nContext.Provider>
  );
}

export function useI18n(): I18nContextValue {
  const ctx = useContext(I18nContext);
  if (!ctx) throw new Error("useI18n must be used within I18nProvider");
  return ctx;
}