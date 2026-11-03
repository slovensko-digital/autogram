import { defineConfig } from "@docmd/core";

// Set by the deploy workflow after scripts/prepare-latest.sh extracts the latest release; local builds show only main.
const latestRelease = process.env.DOCS_LATEST_RELEASE;

const EDGE_BANNER = [
  '<span class="phase-banner" lang="sk"><strong class="idsk-tag">Vývojová verzia</strong><span class="phase-banner__text">',
  'Táto dokumentácia popisuje verziu, ktorá ešte nebola vydaná. <a href="/">Dokumentácia k aktuálnej verzii</a></span></span>',
  '<span class="phase-banner" lang="en"><strong class="idsk-tag">Development version</strong><span class="phase-banner__text">',
  'This documentation describes a version that has not been released yet. <a href="/en/">Documentation of the current version</a></span></span>',
].join('');

export default defineConfig({
  title: 'Autogram', // appended to page titles in <title> and og:title, so it has to fit both languages
  url: process.env.DOCMD_SITE_URL || 'http://localhost:3000',

  // --- Branding ---
  logo: {
    light: 'assets/images/autogram-logo-light.svg',
    dark: 'assets/images/autogram-logo-dark.svg',
    alt: 'Autogram',
    href: '/',
    height: '34px',
  },
  favicon: 'assets/images/favicon.svg',

  // --- Source & Output ---
  src: 'docs',
  out: 'dist',

  redirects: {
    '/sk/': '/',
    '/sk/multi-document/': '/multi-document/',
    '/sk/cli/': '/cli/',
  },

  versions: latestRelease ? {
    current: 'latest',
    all: [
      { id: 'latest', dir: 'latest', label: latestRelease },
      { id: 'edge', dir: 'docs', label: 'edge', banner: { html: EDGE_BANNER, dismissible: false } },
    ],
  } : undefined,

  i18n: {
    default: 'sk',
    position: 'options-menu',
    locales: [
      {
        id: 'sk',
        label: 'Slovenčina',
        translations: {
          skipToContent: 'Preskočiť na hlavný obsah',
          toggleSidebar: 'Prepnúť bočný panel',
          previous: 'Predchádzajúce',
          next: 'Nasledujúce',
          breadcrumb: 'Navigačná cesta',
          onThisPage: 'Na tejto stránke',
          pageNotFound: 'Stránka sa nenašla',
          returnHome: 'Späť na úvod',
          search: 'Hľadať',
          searchPlaceholder: 'Hľadať v dokumentácii...',
          toggleTheme: 'Prepnúť vzhľad',
          selectLanguage: 'Vybrať jazyk',
          language: 'Jazyk',
          editThisPage: 'Upraviť túto stránku',
          permalinkToSection: 'Odkaz na túto sekciu',
          copiedToClipboard: 'Skopírované!',
          copyCode: 'Kopírovať kód',
          mainNavigation: 'Hlavná navigácia',
          translationUnavailable: 'Preklad nie je dostupný',
          fallbackMessage: 'Táto stránka ešte nie je dostupná v jazyku {active}. Zobrazuje sa predvolený jazyk ({default}).',
          copyMarkdown: 'Kopírovať Markdown',
          copyContext: 'Kopírovať kontext',
          close: 'Zavrieť',
          goBack: 'Späť',
          pageNotFoundMsg: 'Požadovaná stránka neexistuje alebo bola presunutá.',
          goToTop: 'Na začiatok stránky',
          print: 'Tlačiť',
          focusMode: 'Režim sústredenia',
          exitFocusMode: 'Ukončiť režim sústredenia',
          footerProductPage: 'Produktová stránka',
          selectVersion: 'Vybrať verziu',
          version: 'Verzia',
          latest: 'Aktuálna',
        },
      },
      {
        id: 'en',
        label: 'English',
        translations: {
          footerProductPage: 'Product website',
        },
      },
    ],
  },

  // --- Layout & UI Architecture ---
  layout: {
    spa: true,
    header: {
      enabled: true,
    },
    sidebar: {
      collapsible: true,
      defaultCollapsed: false,
    },
    optionsMenu: {
      position: 'header',
      components: {
        search: true,      
        themeSwitch: true, 
        sponsor: null,
      }
    },
    footer: {
      style: 'complete',
      description: 'Autogram - podpisovanie a overovanie elektronických dokumentov.',
      columns: [
        {
          title: 'Autogram',
          links: [
            {
              text: 'footerProductPage',
              url: 'https://sluzby.slovensko.digital/autogram',
              external: true,
            },
          ],
        },
      ],
      content: '© Slovensko.Digital',
      branding: false
    }
  },

  // --- Theme Settings ---
  theme: {
    name: 'default',        // Options: 'default', 'sky', 'ruby', 'retro'
    appearance: 'system',   // 'light', 'dark', or 'system'
    codeHighlight: true,
    customCss: [
      '/assets/css/branding.css' // Path relative to site root
    ]
  },

  // --- General Features ---
  minify: true,           
  autoTitleFromH1: true,  
  copyCode: true,         
  pageNavigation: true,   
  
  customJs: ['/assets/js/nav-sections.js', '/assets/js/toc-active.js', '/assets/js/logo-link.js', '/assets/js/top-banner.js'],

  // --- Plugins ---
  plugins: {
    seo: {
      defaultDescription: 'Autogram - používateľská príručka na podpisovanie a overovanie dokumentov / User guide for signing and verifying documents.',
      twitter: { cardType: 'summary_large_image' }
    },
    sitemap: { defaultChangefreq: 'weekly' },
    analytics: {},
    search: {},
    mermaid: {},
    llms: {},
    ai: false,
    okf: false,
    './plugins/browser-language.js': {},
    './plugins/font-preload.js': {},
    './plugins/open-graph.js': {}, // og:site_name, og:locale and the share image per language
    './plugins/edge-noindex.js': {},
  },
  
  // --- Edit Link ---
  editLink: {
    enabled: false,
    text: 'Upraviť túto stránku'
  }
});
