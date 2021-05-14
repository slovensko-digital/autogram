import React from 'react';
import clsx from 'clsx';
import styles from './HomepageFeatures.module.css';

const FeatureList = [
  {
    title: 'Easy to Use',
    Svg: require('../../static/img/features/use.svg').default,
    // TODO: Add signing flow video
    description: (
      <>
        Octosign White Label was designed from the ground up to be easy for use
        exposing only the bare minimum complexity to the end-users.
      </>
    ),
  },
  {
    title: 'Customizable',
    Svg: require('../../static/img/features/customizable.svg').default,
    // TODO: Add documentation links for all three parts
    description: (
      <>
        Whether you want to style the UI to match your branding, change texts,
        or customize the signature parameters, you can do it.
      </>
    ),
  },
  {
    title: 'Completely Free',
    Svg: require('../../static/img/features/free.svg').default,
    description: (
      <>
        Licensed under the open-source MIT license and bundled with compatible software,
        Octosign White Label can be freely modified and used as open-source and proprietary software.
      </>
    ),
  },
];

function Feature({Svg, title, description}) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} alt={title} />
      </div>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
