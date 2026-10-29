// Puts the changelog of main into a release's guides, so that every version of the docs shows the same changelog.
// A release shows the versions up to the latest release: the coming changes and versions not released yet are left
// out. Links point to the latest release's guides, which have every page the changelog mentions, and the page is
// added to the menu of releases tagged before it existed.
import fs from 'fs';
import path from 'path';

const [latestTag, releaseDir] = process.argv.slice(2);
if (!latestTag || !releaseDir) {
    console.error('usage: node scripts/add-changelog.js <latest release tag> <release directory>');
    process.exit(1);
}
const DEFAULT_LOCALE = 'sk';

function version(text) {
    const match = /^v?(\d+(?:\.\d+)*)$/.exec(text.trim());
    return match ? match[1].split('.').map(Number) : null;
}

const latest = version(latestTag);

function isReleased(label) {
    const entry = version(label);
    if (!entry) return false;
    for (let i = 0; i < Math.max(entry.length, latest.length); i++) {
        const difference = (entry[i] ?? 0) - (latest[i] ?? 0);
        if (difference !== 0) return difference < 0;
    }
    return true;
}

for (const locale of fs.readdirSync('docs')) {
    const source = path.join('docs', locale, 'changelog.md');
    const target = path.join(releaseDir, locale);
    if (!fs.existsSync(source) || !fs.existsSync(target)) continue;

    const prefix = locale === DEFAULT_LOCALE ? '' : `/${locale}`;
    const changelog = fs.readFileSync(source, 'utf8')
        .replace(/^== ([^\r\n]*)\r?\n[\s\S]*?(?=^== |^:::)/gm, (entry, label) => isReleased(label) ? entry : '')
        .replace(/\]\((?![a-z]+:|\/|#)([^)#\s]+)\.md(#[^)\s]*)?\)/g,
            (link, page, anchor = '') => `](${prefix}/${page === 'README' ? '' : `${page}/`}${anchor})`);
    fs.writeFileSync(path.join(target, 'changelog.md'), changelog);

    const navigationPath = path.join(target, 'navigation.json');
    const item = JSON.parse(fs.readFileSync(path.join('docs', locale, 'navigation.json'), 'utf8'))
        .find((entry) => entry.path === '/changelog');
    if (!item || !fs.existsSync(navigationPath)) continue;
    const navigation = JSON.parse(fs.readFileSync(navigationPath, 'utf8'));
    if (!JSON.stringify(navigation).includes('"/changelog"')) {
        navigation.push(item);
        fs.writeFileSync(navigationPath, `${JSON.stringify(navigation, null, 2)}\n`);
    }
}
