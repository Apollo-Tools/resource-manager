import Head from 'next/head';
import {siteTitle} from '../components/misc/Sidebar';
import Link from 'next/link';

const Home = () => {
  return (
    <>
      <Head>
        <title>{siteTitle}</title>
      </Head>
      <section>
        <h2>Welcome</h2>
        <ul>
          <li>
            <Link href={`/resources/resources`}>Resources</Link>
          </li>
          <li>
            <Link href={`/deployments/deployments`}>Deployments</Link>
          </li>
          <li>
            <Link href={`/accounts/profile`}>Profile</Link>
          </li>
        </ul>
      </section>
    </>
  );
};

export default Home;
