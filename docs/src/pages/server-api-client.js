import React from 'react';
import Layout from '@theme/Layout';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './server-api-client.module.css';

export default function ServerApi() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={`Server API Client | ${siteConfig.title}`}
      description={siteConfig.tagline}
    >
      <iframe className={styles.iframe} src="https://white-label-client.pages.dev"></iframe>
    </Layout>
  );
}
