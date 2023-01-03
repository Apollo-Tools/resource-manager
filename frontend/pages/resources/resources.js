import Head from 'next/head';
import {siteTitle} from '../../components/Sidebar';
import ResourceTable from '../../components/ResourceTable';
import {Typography} from 'antd';

const Resources = () => {
  return (
    <>
      <Head>
        <title>{`${siteTitle}: Resources`}</title>
      </Head>
      <div className="card container w-11/12 max-w-7xl p-10">
        <Typography.Title level={2}>All Resources</Typography.Title>
        <ResourceTable />
      </div>
    </>
  );
};

export default Resources;
