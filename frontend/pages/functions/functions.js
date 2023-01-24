import Head from 'next/head';
import {siteTitle} from '../../components/Sidebar';
import {Typography} from 'antd';
import FunctionTable from '../../components/FunctionTable';

const Functions = () => {
  return (
    <>
      <Head>
        <title>{`${siteTitle}: Functions`}</title>
      </Head>
      <div className="card container w-11/12 max-w-7xl p-10">
        <Typography.Title level={2}>All Functions</Typography.Title>
        <FunctionTable />
      </div>
    </>
  );
};

export default Functions;
