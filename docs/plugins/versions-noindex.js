// Keeps search engines on the released guides: pages of the unreleased edge version get noindex and are left out of
// sitemap.xml. Local plugins run after docmd's sitemap plugin, so the sitemap already exists here.
import fs from 'fs/promises';
import path from 'path';

const EDGE_PATH = /^([a-z]{2}\/)?edge\//;

export const plugin = {
    name: 'edge-noindex',
    version: '1.0.0',
    capabilities: ['head', 'post-build'],
};

export function generateMetaTags(config, pageContext) {
    const outputPath = (pageContext.outputPath || '').replace(/\\/g, '/');
    return EDGE_PATH.test(outputPath) ? '<meta name="robots" content="noindex">' : '';
}

export async function onPostBuild({ config, outputDir, log }) {
    const sitemapPath = path.join(outputDir, 'sitemap.xml');
    let sitemap;
    try {
        sitemap = await fs.readFile(sitemapPath, 'utf8');
    } catch {
        return;
    }
    const siteUrl = (config.url || '').replace(/\/$/, '');
    const urls = sitemap.match(/  <url>[\s\S]*?<\/url>\n/g) || [];
    const edgeUrls = urls.filter((url) => {
        const loc = url.match(/<loc>([^<]*)<\/loc>/)?.[1] || '';
        return EDGE_PATH.test(loc.replace(siteUrl, '').replace(/^\//, ''));
    });
    if (edgeUrls.length === 0) return;
    await fs.writeFile(sitemapPath, edgeUrls.reduce((xml, url) => xml.replace(url, ''), sitemap));
    log?.(`Removed ${edgeUrls.length} edge pages from the sitemap`);
}
