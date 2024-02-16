import Head from 'next/head';
import {siteTitle} from '../components/misc/Sidebar';
import HomeOverview from '../components/misc/HomeOverview';

const Home = () => {
  return (
    <>
      <Head>
        <title>{siteTitle}</title>
      </Head>
      <div className="default-card">
        <HomeOverview />
      </div>
    </>
  );
};

export default Home;
