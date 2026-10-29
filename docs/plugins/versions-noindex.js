// Keeps search engines on the guides of the latest release: pages of the other versions (edge and older releases) get
// noindex and are left out of sitemap.xml. Local plugins run after docmd's sitemap plugin, so the sitemap already
// exists here.
import fs from 'fs/promises';
import path from 'path';

export const plugin = {
    name: 'versions-noindex',
    version: '1.0.0',
    capabilities: ['head', 'post-build'],
};

function otherVersionsPath(config) {
    const ids = (config.versions?.all || []).map((v) => v.id).filter((id) => id !== config.versions.current);
    if (ids.length === 0) return null;
    return new RegExp(`^([a-z]{2}/)?(${ids.map((id) => id.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')).join('|')})/`);
}

export function generateMetaTags(config, pageContext) {
    const outputPath = (pageContext.outputPath || '').replace(/\\/g, '/');
    return otherVersionsPath(config)?.test(outputPath) ? '<meta name="robots" content="noindex">' : '';
}

export async function onPostBuild({ config, outputDir, log }) {
    const otherVersions = otherVersionsPath(config);
    if (!otherVersions) return;
    const sitemapPath = path.join(outputDir, 'sitemap.xml');
    let sitemap;
    try {
        sitemap = await fs.readFile(sitemapPath, 'utf8');
    } catch {
        return;
    }
    const siteUrl = (config.url || '').replace(/\/$/, '');
    const urls = sitemap.match(/  <url>[\s\S]*?<\/url>\n/g) || [];
    const otherUrls = urls.filter((url) => {
        const loc = url.match(/<loc>([^<]*)<\/loc>/)?.[1] || '';
        return otherVersions.test(loc.replace(siteUrl, '').replace(/^\//, ''));
    });
    if (otherUrls.length === 0) return;
    await fs.writeFile(sitemapPath, otherUrls.reduce((xml, url) => xml.replace(url, ''), sitemap));
    log?.(`Removed ${otherUrls.length} pages of other versions from the sitemap`);
}
