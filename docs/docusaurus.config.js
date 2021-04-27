/** @type {import('@docusaurus/types').DocusaurusConfig} */
module.exports = {
  title: 'Octosign White Label',
  tagline: 'Customizable, simple, cross-platform (Windows, macOS, Linux) desktop app that can be used to create signatures compliant with the eIDAS Regulation and be integrated with your (web) application.',
  url: 'https://whitelabel.octosign.com',
  baseUrl: '/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',
  organizationName: 'octosign',
  projectName: 'white-label',
  themeConfig: {
    navbar: {
      title: 'Octosign White Label',
      logo: {
        alt: 'Octosign Logo',
        src: 'img/icon.svg',
      },
      items: [
        {
          type: 'doc',
          docId: 'intro',
          position: 'left',
          label: 'Documentation',
        },
        {
          to: 'server-api',
          position: 'left',
          label: 'Server API',
        },
        {
          href: 'https://github.com/octosign/white-label',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    respectPrefersColorScheme: true,
    footer: {
      style: 'light',
      copyright: `Copyright © 2021 <a href="https://duras.me">Jakub Duras</a> and <a href="https://github.com/octosign/white-label/graphs/contributors">Contributors</a>. Built with Docusaurus.`,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          // Please change this to your repo.
          editUrl:
            'https://github.com/octosign/white-label/edit/master/docs/',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
};
 