// Preloads the Source Sans Pro webfonts from branding.css, so that the text is laid out in them right away instead of
// switching from the fallback font after the page is shown (headings visibly jumped on every page load).
// The URLs must resolve to the same files as the @font-face rules in assets/css/branding.css.
const FONTS = ['SourceSansPro-Regular.woff', 'SourceSansPro-Bold.woff'];

export const plugin = {
    name: 'font-preload',
    version: '1.0.0',
    capabilities: ['head'],
};

export function generateMetaTags(config, pageContext, root) {
    const base = root || './';
    return FONTS
        .map((font) => `<link rel="preload" href="${base}assets/fonts/${font}" as="font" type="font/woff" crossorigin>`)
        .join('\n');
}
