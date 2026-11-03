// Sends a visitor who comes from another site to a Slovak page to the same page in English, unless the browser prefers
// Slovak or Czech or the visitor has picked a language in the switcher (docmd-locale). Crawlers are not redirected, so
// the Slovak pages stay in search results. GitHub Pages has no server side, so it is a script in <head>.
const TARGET_LOCALE = 'en';

const SCRIPT = `<script>(function () {
  try { if (localStorage.getItem('docmd-locale')) return; } catch (e) {}
  if (document.referrer.indexOf(location.origin + '/') === 0) return;
  if (/bot|crawl|spider|slurp|google|lighthouse/i.test(navigator.userAgent)) return;
  var languages = (navigator.languages && navigator.languages.length ? navigator.languages : [navigator.language]).filter(Boolean);
  if (!languages.length || languages.some(function (language) { return /^(sk|cs)\\b/i.test(language); })) return;
  location.replace('/${TARGET_LOCALE}' + location.pathname + location.search + location.hash);
})();</script>`;

export const plugin = {
    name: 'browser-language',
    version: '1.0.0',
    capabilities: ['head'],
};

export function generateMetaTags(config, pageContext) {
    const outputPath = (pageContext.outputPath || '').replace(/\\/g, '/');
    const otherLocales = (config.i18n?.locales || []).map((locale) => locale.id).filter((id) => id !== config.i18n.default);
    if (outputPath === '404.html' || otherLocales.some((id) => outputPath.startsWith(`${id}/`))) {
        return '';
    }
    return SCRIPT;
}
