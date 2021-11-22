import React from 'react';
import clsx from 'clsx';
import styles from './HomepageFeatures.module.css';
import ThemedImage from '@theme/ThemedImage';
import useBaseUrl from '@docusaurus/useBaseUrl';

const FeatureList = [
  {
    title: 'Plays nicely with Cats',
    img: "img/cat.png",
    darkImg: "img/catdark.png",
    alt: "bad drawing of a cat",
    description: (
      <>
        Provides type classes and integrations with Cats.
      </>
    ),
  },
  {
    title: 'Tagless',
    img: "img/tagless.png",
    darkImg: "img/taglessdark.png",
    alt: "drawing of some square brackets around an underscore, representing tagless",
    description: (
      <>
        Provides tagless interfaces, and integration with cats-tagless.
      </>
    ),
  },
  {
    title: 'Built on http4s',
    img: "img/http4s.png",
    darkImg: "img/http4sdark.png",
    alt: "drawing of the http4s logo, a box with  h, 4 and s on the sides",
    description: (
      <>
        Built on http4s for all HTTP calls.
      </>
    ),
  },
];

function Feature({img, darkImg, alt, title, description}) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <ThemedImage alt={alt} sources={{light: useBaseUrl(img), dark: useBaseUrl(darkImg) }}/>
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
