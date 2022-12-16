import Head from 'next/head'
import Layout, {siteTitle} from "../components/layout";
import utilStyles from '../styles/utils.module.css';
import {getSortedPostsData} from "../lib/posts";
import Date from "../components/date";
import Link from "next/link";
import {Button, Space} from 'antd';
import {login} from "../lib/accounts";
export async function getStaticProps() {
    const allPostsData = getSortedPostsData();
    return {
        props: {
            allPostsData
        }
    }
}

export default function Home({allPostsData}) {
    const onClickLogout = () => {
        login("user1", "password1")
            .then(r => console.log("finished"));
    }

    return (
        <Layout home>
            <Head>
                <title>{siteTitle}</title>
            </Head>
            <section className={utilStyles.headingMd}>
                <p>This is the introduction to me</p>
                <p>
                    (This is a sample website - you’ll be building a site like this on{' '}
                    <a href="https://nextjs.org/learn">our Next.js tutorial</a>.)
                </p>
            </section>
            <section className={`${utilStyles.headingMd} ${utilStyles.padding1px}`}>
                <h2 className={utilStyles.headingLg}>Blog</h2>
                <ul className={utilStyles.list}>
                    {allPostsData.map(({id, date, title}) => (
                        <li className={utilStyles.listItem} key={id}>
                            <Link href={`/posts/${id}`}>{title}</Link>
                            <br/>
                            <small className={utilStyles.lightText}>
                                <Date dateString={date}/>
                            </small>
                        </li>
                    ))}
                </ul>
            </section>
            <section>

                <p>
                    <Link href="/accounts/login">
                        <Button type="primary">Login</Button>
                    </Link>
                    <Button type="primary">Logout</Button>
                </p>
            </section>
        </Layout>
    );
}