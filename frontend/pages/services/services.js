import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {Button, Typography} from 'antd';
import ServiceTable from '../../components/services/ServiceTable';
import Link from 'next/link';
import {PlusOutlined} from '@ant-design/icons';

const Services = () => {
  return (
    <>
      <Head>
        <title>{`${siteTitle}: Services`}</title>
      </Head>
      <div className="card container w-11/12 max-w-7xl p-10">
        <Typography.Title level={2}>All Services</Typography.Title>
        <Link href={`/services/new-service`} className="block mb-5">
          <Button type="default" icon={<PlusOutlined />} >New Service</Button>
        </Link>
        <ServiceTable />
      </div>
    </>
  );
};

export default Services;
