// Adds the Open Graph tags that docmd's seo plugin does not write: og:site_name, og:locale and a share image per
// language (assets/images/og-image-<locale>.png, 1200x630) with its size and alt text, also for Twitter/X.
// The seo plugin's own image (openGraph.defaultImage or `image` in frontmatter) must stay unset, or the tags repeat.
const SITE_NAME = 'Autogram';
const IMAGE = { width: 1200, height: 630, type: 'image/png' };
const LOCALES = {
    sk: {
        ogLocale: 'sk_SK',
        imageAlt: 'Autogram - používateľská príručka na podpisovanie a overovanie elektronických dokumentov',
    },
    en: {
        ogLocale: 'en_GB',
        imageAlt: 'Autogram user guide for signing and verifying electronic documents',
    },
};

export const plugin = {
    name: 'open-graph',
    version: '1.0.0',
    capabilities: ['head'],
};

const esc = (value) => String(value).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;');

function pageLocale(config, outputPath) {
    const firstSegment = (outputPath || '').replace(/\\/g, '/').split('/')[0];
    return LOCALES[firstSegment] ? firstSegment : config.i18n?.default || 'sk';
}

export function generateMetaTags(config, pageContext) {
    const { frontmatter = {}, outputPath, urlContext } = pageContext;
    if (frontmatter.components?.meta === false || frontmatter.noindex || frontmatter.seo?.noindex) {
        return '';
    }

    const locale = pageLocale(config, outputPath);
    const { ogLocale, imageAlt } = LOCALES[locale];
    const tags = [
        ['property', 'og:site_name', SITE_NAME],
        ['property', 'og:locale', ogLocale],
        ...Object.keys(LOCALES)
            .filter((id) => id !== locale)
            .map((id) => ['property', 'og:locale:alternate', LOCALES[id].ogLocale]),
    ];

    // Crawlers need an absolute image URL, so there is none without the site URL (offline builds).
    const siteUrl = (config.url || '').replace(/\/$/, '');
    if (siteUrl && !urlContext?.offline) {
        const image = `${siteUrl}/assets/images/og-image-${locale}.png`;
        tags.push(
            ['property', 'og:image', image],
            ['property', 'og:image:type', IMAGE.type],
            ['property', 'og:image:width', IMAGE.width],
            ['property', 'og:image:height', IMAGE.height],
            ['property', 'og:image:alt', imageAlt],
            ['name', 'twitter:image', image],
            ['name', 'twitter:image:alt', imageAlt],
        );
    }

    return tags.map(([attr, key, value]) => `<meta ${attr}="${key}" content="${esc(value)}">`).join('\n');
}
