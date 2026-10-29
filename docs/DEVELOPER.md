# Autogram - Documentation

This directory is a standalone docmd preview of the copied Autogram user guide.
It includes the Slovak and English introductions and two topics in each language.

## Running locally

```console
cd docs
npm ci
npm run dev
```

Requires Node.js 20 or newer. Open <http://localhost:3000>. The development server
rebuilds the documentation when the content or configuration changes. Restart it
after upgrading docmd.

The dependency tree is pinned in `package-lock.json`. Use `npm ci` for routine
setup; use `npm install` to update the lockfile after intentional dependency changes.

## Static build

```console
npm run build
npm run validate
```

The generated site is in `dist/`. Canonical links, the sitemap, `robots.txt` and `llms.txt`
use `DOCMD_SITE_URL` (`http://localhost:3000` when unset):

```console
DOCMD_SITE_URL=https://docs.autogram.slovensko.digital npm run build
```

## Versions

The published site has these versions with a switcher at the top of the sidebar:

- the latest release (labelled with its tag, e.g. `v2.8.5`) at `/` and `/en/`, which is what
  users of the released application read,
- the releases that were the latest before it, since v2.8.3, at `/<tag>/` and `/en/<tag>/`
  (e.g. `/v2.8.3/`), with a banner saying the version is no longer the current one,
- `edge`, the guides from `main`, at `/edge/` and `/en/edge/`, with a banner saying the version
  has not been released yet.

`plugins/versions-noindex.js` marks the pages of all versions but the latest release `noindex`
and removes them from `sitemap.xml`, so search engines show only the latest release.

A banner is an ID-SK phase banner (`govuk-phase-banner` with a `govuk-tag`) across the top of
the window, on the light grey of the ID-SK header's top bar. The version switcher looks like the
language menu of the ID-SK header: a text button with an arrow and a plain list with the chosen
version bold and underlined. Both are styled in `branding.css`.

The releases' guides are not kept in the repository. `scripts/prepare-releases.sh <tag>...`
extracts `docs/` of each tag into `releases/<tag>/` and its `assets/images/` into
`assets/releases/<tag>/images/`, and points the image paths in the extracted guides there. A
release therefore keeps its own screenshots when `main` replaces or deletes them. Configuration,
styles, scripts and plugins come from `main` for all versions. The versions are turned on only
when `DOCS_RELEASES` (the tags, the latest first, separated by commas) is set, so `npm run dev`
shows just the guides from `main`. To build the whole site locally:

```console
bash scripts/prepare-releases.sh v2.8.5 v2.8.3
DOCS_RELEASES=v2.8.5,v2.8.3 npm run build
npm run check-images
```

`npm run check-images` fails when a page of the built site shows an image that is not in it.
A fix to the guides on `main` reaches a release's version only with the next release.

## Deployment

`.github/workflows/deploy_docs.yaml` builds, validates and publishes the site to GitHub Pages
at <https://docs.autogram.slovensko.digital> on every push to `main` that changes `docs/`, and
when a release is published or edited (or a pre-release becomes a release), so that `/` switches
to the guides of the release marked as the latest. Marking a published release as the latest is
an edit. It takes the latest release from the GitHub API and the releases that were the latest
before it from `releases.json`, which it publishes in the root of the site. GitHub does not keep
which releases were the latest, so the published site is the only record of it: a release that
was published but never marked as the latest is not a version of the docs, and a release deleted
on GitHub is left out. When `releases.json` is not published yet, the site starts with the latest
release only. When it cannot be read, the workflow fails rather than publish the site without the
older releases. A release event runs the workflow file from the released tag, so a release
tagged before its event was added to the triggers does not start it. Its ref is the tag, so the
`github-pages` environment allows deployments from tags `v*` besides `main` (Settings →
Environments → github-pages). It can also be run manually from the Actions tab.
`DOCMD_SITE_URL` comes from the repository's Pages settings (the custom domain), so the domain
is not repeated in the workflow.

## Moving to another project

This directory is self-contained and can be moved or renamed without changes to
the parent project. Keep the source files, assets, `plugins/`, configuration, `package.json`,
`package-lock.json` and `.gitignore`. Do not copy `node_modules/` or `dist/`; in the
destination directory, run `npm ci` followed by `npm run dev` or `npm run build`.

## Content and configuration

- `docs/sk/` and `docs/en/` contain the guides. Native docmd i18n serves Slovak
  (the default locale) at `/` and English at `/en/`. The previous `/sk/` URLs
  redirect to the corresponding default-locale pages.
- The language switcher is in the header next to search and the theme toggle.
  Switching languages preserves the current topic.
- `assets/images/screenshots/<locale>/` contains the guide screenshots. Reference
  them as `/assets/images/screenshots/<locale>/<filename>.png` in Markdown.
  Docmd copies these assets automatically; no local plugin is needed.
- `docmd.config.js` defines branding, native `i18n` settings and Slovak UI translations.
- `layout.footer.columns` in that configuration defines the Autogram product-page
  link; its label is localized through each locale's `footerProductPage` translation.
- `docs/sk/navigation.json` and `docs/en/navigation.json` define the menu for each
  language. Menu paths do not include locale prefixes. Add new chapters to both
  navigation lists and both locale directories.
- Explicit heading IDs (`{#for-integrators}` and `{#pre-integratorov}`) keep the
  CLI links to the API sections stable despite docmd's generated heading IDs.

## Branding

The documentation's `assets/css/branding.css` adapts docmd's native components to
[ID-SK 2 typography](https://idsk2.gov.sk/komponenty/typografia) and its
[color palette](https://idsk2.gov.sk/komponenty/farby): Source Sans Pro, a 19 px
desktop body (16 px on small screens), blue underlined content links, square
controls and yellow keyboard focus. Sizes are expressed in relative units.
The side navigation follows the navigation used on the
[GOV.UK Design System website](https://design-system.service.gov.uk/components/accordion/history/):
equally sized links, indented sub-items, a blue rail along the whole active branch and a bold,
underlined current page, without filled backgrounds. Sections retain their collapsible behavior
and small chevrons. The active table-of-contents link is also bold and underlined, without a filled
background. Callouts follow ID-SK 2 / GOV.UK content components: `info` is the inset text, `warning`
is the warning text with the "!" icon. Regular text, callouts and step containers share the same
75ch maximum width.
Primary call-to-action links use docmd's native `::: button` component, styled after the
[GOV.UK button](https://design-system.service.gov.uk/components/button/) with
the classic green primary background, white text, a bottom shadow and the shared yellow
keyboard focus. They stay green in both themes and fill the available width on small screens.
Both installation guides
link directly to the product website's download section.
The docmd navigation, search, language switcher and theme switch remain native.
Custom JavaScript (`customJs` in `docmd.config.js`):

- `assets/js/nav-sections.js`: docmd renders collapsible sidebar sections as plain `<span>`s,
  the script makes them buttons operable with the keyboard and screen readers and marks the
  current link with `aria-current="page"`. docmd also centres the current link in the sidebar
  100 ms after every page load, which made the menu jump; the script brings the link into view
  right away and only when it is hidden, and ignores docmd's centring otherwise.
- `assets/js/toc-active.js`: docmd's scroll spy marks a table-of-contents link only when its
  heading crosses a band at 15–20 % of the window height. A heading reached through its link
  stops at its `scroll-margin-top`, which is above that band in taller windows or with instant
  scrolling (reduced motion), and the last sections never get there, so clicking a section did
  not make it active. The script decides the active link itself: the jumped-to section until
  the user scrolls, otherwise the last heading scrolled to the top, or the last one at the page end.
- `assets/js/logo-link.js`: docmd links the logo with a path relative to the current page and
  does not update it on SPA navigation, so after moving from the home page to a subpage the
  logo pointed to that subpage. The script turns the link into an absolute path on load.
- `assets/js/top-banner.js`: docmd renders the top banner (the notice of `edge` and of older
  releases) in the content column, while the sidebar is fixed to the top of the window.
  `branding.css` places the banner across the whole window, like docmd's top menubar, and moves
  the sidebar, the sticky header and the page down by its height, which the script keeps up to date.

`plugins/font-preload.js` is a local docmd plugin (listed in `plugins`) that adds
`<link rel="preload">` for both webfonts. Together with `font-display: fallback` in
`branding.css`, the text is laid out in Source Sans Pro from the first paint instead of
switching from the fallback font, which made the headings jump on every page load.

`plugins/open-graph.js` adds the Open Graph tags docmd's seo plugin leaves out: `og:site_name`,
`og:locale` with the other language as `og:locale:alternate`, and the share image for the
page's language, `assets/images/og-image-sk.png` or `og-image-en.png` (1200×630 PNG, with its
size and alt text, also as `twitter:image`). The image URL is absolute, so links shared from a
build without `DOCMD_SITE_URL` point to `localhost`. Leave `openGraph.defaultImage` in the seo
plugin and `image` in page frontmatter unset, otherwise the image tags appear twice.

Every page has a `description` in its frontmatter, in the page's language. docmd uses it for
`<meta name="description">`, `og:description` and `llms.txt`; a page without one falls back to
the bilingual `defaultDescription`, so give new pages their own. `title` in the configuration is
just `Autogram`, because docmd appends it to every page title in both languages
("Viac dokumentov naraz - Autogram"). The home pages set `titleAppend: false`, as their heading
already names Autogram.
There is no additional UI framework. Dark mode is a
contrasting adaptation, not an official ID-SK dark theme. This is visual alignment,
not a claim of certified compliance or an official public-administration website.
Code token colors follow the active `data-theme` directly, including after reloads
and SPA navigation, with contrasting colors for the themed code backgrounds.

The regular and bold Source Sans Pro webfonts are hosted locally in `assets/fonts/`
and sourced from the ID-SK 2 site's public font assets. Their SIL Open Font License
is included in `assets/fonts/OFL.txt`. No external font requests are needed.

The light and dark wordmarks in `assets/images/autogram-logo-light.svg` and
`assets/images/autogram-logo-dark.svg` use **Source Sans Pro Bold**, matching
the desktop application's logo and Autogram Portal. The proportions and underline
follow the [desktop application's styles](https://github.com/slovensko-digital/autogram/blob/main/src/main/resources/digital/slovensko/autogram/ui/gui/idsk.css).
The default double-storey `g` has a closed lower loop. Letters are converted to
vector outlines, so the SVGs need no external font files or network requests.
The light logo uses `#0B0C0C` lettering and the application's yellow `#FFDD00`
underline, which sits behind the bottom of the `g`, as in the original logo.
The dark logo uses white lettering and a white underline. The matching favicon
uses the same outlined `A`, always with light-logo colors regardless of the theme.

Only the Autogram wordmarks, favicon, share images and shared Autogram screenshots are kept as
project image assets. `node_modules/` contains required docmd dependencies and
`dist/` is generated output, not documentation source.

The edit link is disabled. Enable `editLink` and set its `baseUrl` to
the actual Markdown source directory after moving this setup to its final repository.
