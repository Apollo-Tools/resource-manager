import {useState} from 'react';
import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {Divider, Segmented, Switch, Typography} from 'antd';
import ServiceTable from '../../components/services/ServiceTable';
import NewEntityButton from '../../components/misc/NewEntityButton';
import ArtifactTypeTable from '../../components/artifacttypes/ArtifactTypeTable';
import PropTypes from 'prop-types';

const Services = ({setError}) => {
  const [selectedSegment, setSelectedSegment] = useState('Services');
  const segments = ['Services', 'Types'];
  const [showAllServices, setShowAllServices] = useState(false);
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
                  checkedChildren="all service"
                  unCheckedChildren="my services"
                  checked={showAllServices}
                  onChange={setShowAllServices}
                />
              </div>
              <div className="col-span-full">
                <ServiceTable allServices={showAllServices} setError={setError} />
              </div>
            </div> :
          <>
            <NewEntityButton name="Service Type" path={`/services/new-type`}/>
            <ArtifactTypeTable artifact="service" setError={setError} />
          </>
        }
      </div>
    </>
  );
};

Services.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default Services;
