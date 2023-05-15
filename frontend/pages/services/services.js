import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {Typography} from 'antd';
import ServiceTable from '../../components/services/ServiceTable';
import NewEntityButton from '../../components/misc/NewEntityButton';

const Services = () => {
  return (
    <>
      <Head>
        <title>{`${siteTitle}: Services`}</title>
      </Head>
      <div className="card container w-11/12 max-w-7xl p-10">
        <Typography.Title level={2}>All Services</Typography.Title>
        <NewEntityButton name="Service" />
        <ServiceTable />
      </div>
    </>
  );
};

export default Services;
