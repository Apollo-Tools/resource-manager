import Head from 'next/head';
import {siteTitle} from '../components/Sidebar';
import Link from 'next/link';

export default function Home() {
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
            <Link href={`/reservations/reservations`}>Reservations</Link>
          </li>
          <li>
            <Link href={`/accounts/profile`}>Profile</Link>
          </li>
        </ul>
      </section>
    </>
  );
}
