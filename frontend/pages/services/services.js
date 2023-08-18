import {useState} from 'react';
import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {Divider, Segmented, Typography} from 'antd';
import ServiceTable from '../../components/services/ServiceTable';
import NewEntityButton from '../../components/misc/NewEntityButton';
import ArtifactTypeTable from '../../components/artifacttypes/ArtifactTypeTable';

const Services = () => {
  const [selectedSegment, setSelectedSegment] = useState('Services');
  const segments = ['Services', 'Types'];
  return (
    <>
      <Head>
        <title>{`${siteTitle}: Services`}</title>
      </Head>
      <div className="default-card">
        <Typography.Title level={2}>All Services</Typography.Title>
        <Segmented options={segments} value={selectedSegment}
          onChange={(e) => setSelectedSegment(e)} size="large" block/>
        <Divider />
        {selectedSegment === 'Services' ?
          <>
            <NewEntityButton name="Service" />
            <ServiceTable />
          </> :
          <>
            <NewEntityButton name="Service Type" path={`/services/new-type`}/>
            <ArtifactTypeTable artifact="service" />
          </>
        }
      </div>
    </>
  );
};

export default Services;
