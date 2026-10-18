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

## Deployment

`.github/workflows/deploy_docs.yaml` builds, validates and publishes the site to GitHub Pages
at <https://docs.autogram.slovensko.digital> on every push to `main` that changes `docs/`.
It can also be run manually from the Actions tab. `DOCMD_SITE_URL` comes from the repository's
Pages settings (the custom domain), so the domain is not repeated in the workflow.

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

`plugins/font-preload.js` is a local docmd plugin (listed in `plugins`) that adds
`<link rel="preload">` for both webfonts. Together with `font-display: fallback` in
`branding.css`, the text is laid out in Source Sans Pro from the first paint instead of
switching from the fallback font, which made the headings jump on every page load.
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

Only the Autogram wordmarks, favicon and shared Autogram screenshots are kept as
project image assets. `node_modules/` contains required docmd dependencies and
`dist/` is generated output, not documentation source.

The edit link is disabled. Enable `editLink` and set its `baseUrl` to
the actual Markdown source directory after moving this setup to its final repository.
