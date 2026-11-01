// Fails when a page of the built site shows an image that is not in the site, for example a screenshot that main
// removed but the released version's guides still use.
import fs from 'fs';
import path from 'path';

const dist = path.resolve(process.argv[2] || 'dist');
const missing = [];

for (const file of fs.readdirSync(dist, { recursive: true })) {
    if (!file.endsWith('.html')) continue;
    const html = fs.readFileSync(path.join(dist, file), 'utf8');
    for (const match of html.matchAll(/<img\b[^>]*?\ssrc=(?:"([^"]*)"|'([^']*)'|([^\s>]+))/g)) {
        const src = match[1] ?? match[2] ?? match[3];
        if (/^([a-z]+:|\/\/)/i.test(src)) continue;
        const target = decodeURI(src.split(/[?#]/)[0]);
        const resolved = target.startsWith('/') ? path.join(dist, target) : path.resolve(dist, path.dirname(file), target);
        if (!fs.existsSync(resolved)) missing.push(`${file.replace(/\\/g, '/')}: ${src}`);
    }
}

if (missing.length > 0) {
    console.error(`Missing images:\n${missing.join('\n')}`);
    process.exit(1);
}
console.log('All images exist.');
