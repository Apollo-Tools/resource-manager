import AccountTable from '../../components/accounts/AccountTable';
import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {Typography} from 'antd';
import NewEntityButton from '../../components/misc/NewEntityButton';


const Accounts = () => {
  return <>
    <Head>
      <title>{`${siteTitle}: Accounts`}</title>
    </Head>
    <div className="default-card">
      <Typography.Title level={2}>Accounts</Typography.Title>
      <NewEntityButton name="Account"/>
      <AccountTable />
    </div>
  </>;
};

export default Accounts;
