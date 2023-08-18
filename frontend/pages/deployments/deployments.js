import {siteTitle} from '../../components/misc/Sidebar';
import Head from 'next/head';
import {Typography} from 'antd';
import DeploymentTable from '../../components/deployments/DeploymentTable';
import NewEntityButton from '../../components/misc/NewEntityButton';

const Deployments = () => {
  return (
    <>
      <Head>
        <title>{`${siteTitle}: Deployments`}</title>
      </Head>
      <div className="default-card">
        <Typography.Title level={2}>My Deployments</Typography.Title>
        <NewEntityButton name="Deployment"/>
        <DeploymentTable />
      </div>
    </>
  );
};

export default Deployments;
