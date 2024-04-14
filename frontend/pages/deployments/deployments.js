import {siteTitle} from '../../components/misc/Sidebar';
import Head from 'next/head';
import {Typography} from 'antd';
import DeploymentTable from '../../components/deployments/DeploymentTable';
import NewEntityButton from '../../components/misc/NewEntityButton';
import PropTypes from 'prop-types';

const Deployments = ({setError}) => {
  return (
    <>
      <Head>
        <title>{`${siteTitle}: Deployments`}</title>
      </Head>
      <div className="default-card">
        <Typography.Title level={2}>My Deployments</Typography.Title>
        <NewEntityButton name="Deployment"/>
        <DeploymentTable setError={setError}/>
      </div>
    </>
  );
};

Deployments.propTypes = {
  setError: PropTypes.func.isRequired,
};


export default Deployments;
