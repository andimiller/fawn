// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'fawn',
  tagline: 'Functional Amazon Webservices Negotiator',
  url: 'https://your-docusaurus-test-site.com',
  baseUrl: '/fawn/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',
  organizationName: 'meltwater', // Usually your GitHub org/user name.
  projectName: 'fawn', // Usually your repo name.

  presets: [
    [
      '@docusaurus/preset-classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          path: "../docs-builder/target/mdoc",
          editUrl: 'https://github.com/meltwater/fawn/edit/main/docs/',
          remarkPlugins: [[require('mdx-mermaid'), { mermaid: {
            theme: 'dark',
            themeVariables: {primaryColor: '#e5aa70'},
            securityLevel:'loose',
            flowchart: { "htmlLabels": true }
	  }}]],
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        title: 'Fawn',
        logo: {
          alt: 'Logo-sized picture of a fawn',
          src: 'img/logo.svg',
          srcDark: 'img/logodark.svg',
        },
        items: [
          {
            type: 'doc',
            docId: 'intro',
            position: 'left',
            label: 'Introduction',
          },
          {
            type: 'doc',
            docId: 'SQS/making-a-client',
            position: 'left',
            label: 'SQS',
          },
          /*{to: '/blog', label: 'Blog', position: 'left'}, */
          {
            href: 'https://github.com/meltwater/fawn',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Docs',
            items: [
              {
                label: 'Introduction',
                to: '/docs/intro',
              },
            ],
          },
          {
            title: 'Community',
            items: [
              {
                label: 'Twitter',
                href: 'https://twitter.com/meltwatereng',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} Meltwater. Built with Docusaurus.`,
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
        additionalLanguages: ['java','scala'],
      },
    }),
};

module.exports = config;
