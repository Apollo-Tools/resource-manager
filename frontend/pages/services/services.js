import {useState} from 'react';
import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {Divider, Segmented, Switch, Typography} from 'antd';
import ServiceTable from '../../components/services/ServiceTable';
import NewEntityButton from '../../components/misc/NewEntityButton';
import ArtifactTypeTable from '../../components/artifacttypes/ArtifactTypeTable';

const Services = () => {
  const [selectedSegment, setSelectedSegment] = useState('Services');
  const segments = ['Services', 'Types'];
    const [showPublicServices, setShowPublicServices] = useState(false);
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
            <div className="grid grid-cols-6 gap-5 content-center">
                <div className="grid col-span-1 content-center">
                    <NewEntityButton name="Service" marginBottom={false}/>
                </div>
                <div className="grid col-start-6 col-span-1 content-center justify-end">
                    <Switch
                        checkedChildren="public service"
                        unCheckedChildren="own services"
                        checked={showPublicServices}
                        onChange={setShowPublicServices}
                    />
                </div>
                <div className="col-span-full">
                    <ServiceTable publicServices={showPublicServices}/>
                </div>
            </div> :
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
