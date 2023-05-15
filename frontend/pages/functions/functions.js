import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {Typography} from 'antd';
import FunctionTable from '../../components/functions/FunctionTable';
import NewEntityButton from '../../components/misc/NewEntityButton';

const Functions = () => {
  return (
    <>
      <Head>
        <title>{`${siteTitle}: Functions`}</title>
      </Head>
      <div className="card container w-11/12 max-w-7xl p-10">
        <Typography.Title level={2}>All Functions</Typography.Title>
        <NewEntityButton name="Function" />
        <FunctionTable />
      </div>
    </>
  );
};

export default Functions;
